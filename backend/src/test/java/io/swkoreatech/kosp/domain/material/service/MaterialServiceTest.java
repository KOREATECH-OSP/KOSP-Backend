package io.swkoreatech.kosp.domain.material.service;

import static io.swkoreatech.kosp.global.common.fixture.TestUserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.material.dto.response.MaterialFolderResponse;
import io.swkoreatech.kosp.domain.material.dto.response.MaterialItemResponse;
import io.swkoreatech.kosp.domain.material.model.FolderType;
import io.swkoreatech.kosp.domain.material.model.MaterialFolder;
import io.swkoreatech.kosp.domain.material.model.MaterialItem;
import io.swkoreatech.kosp.domain.material.model.MaterialSource;
import io.swkoreatech.kosp.domain.material.model.Visibility;
import io.swkoreatech.kosp.domain.material.repository.MaterialFolderRepository;
import io.swkoreatech.kosp.domain.material.repository.MaterialItemRepository;
import io.swkoreatech.kosp.domain.upload.client.S3StorageClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("MaterialService 단위 테스트 - 공개 자료 노출 정책")
class MaterialServiceTest {

    private static final Long OWNER_ID = 1L;

    @InjectMocks
    private MaterialService materialService;

    @Mock
    private MaterialFolderRepository folderRepository;

    @Mock
    private MaterialItemRepository itemRepository;

    @Mock
    private S3StorageClient s3StorageClient;

    private final User owner = createUser(OWNER_ID);

    private MaterialFolder folder(Long id, MaterialFolder parent, Visibility visibility) {
        MaterialFolder f = MaterialFolder.builder()
            .user(owner)
            .parent(parent)
            .name("폴더" + id)
            .folderType(FolderType.CUSTOM)
            .source(MaterialSource.MANUAL)
            .visibility(visibility)
            .isStartFolder(false)
            .sortOrder(0)
            .build();
        ReflectionTestUtils.setField(f, "id", id);
        return f;
    }

    private MaterialItem item(Long id, MaterialFolder parent, Visibility visibility, Integer semesterOrder) {
        MaterialItem i = MaterialItem.builder()
            .folder(parent)
            .user(owner)
            .title("자료" + id)
            .source(MaterialSource.MANUAL)
            .fileUrl("https://cdn.example.com/material/" + id + ".pdf")
            .originalFileName(id + ".pdf")
            .visibility(visibility)
            .semesterOrder(semesterOrder)
            .materialDate(LocalDateTime.of(2026, 3, 1, 0, 0))
            .build();
        ReflectionTestUtils.setField(i, "id", id);
        return i;
    }

    @Nested
    @DisplayName("getPublicFolders 메서드")
    class GetPublicFoldersTest {

        @Test
        @DisplayName("상위 폴더가 비공개면 하위 공개 폴더도 노출하지 않는다")
        void hidesPublicChild_whenParentIsPrivate() {
            // given: PRIVATE(부모) > PUBLIC(자식)
            MaterialFolder parent = folder(1L, null, Visibility.PRIVATE);
            MaterialFolder child = folder(2L, parent, Visibility.PUBLIC);
            given(folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(OWNER_ID))
                .willReturn(List.of(parent, child));

            // when
            List<MaterialFolderResponse> result = materialService.getPublicFolders(OWNER_ID);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("itemCount 에는 공개 자료 수만 포함한다")
        void countsOnlyPublicItems() {
            // given: 공개 폴더 안에 공개 1건 + 개별 비공개 1건
            MaterialFolder pub = folder(1L, null, Visibility.PUBLIC);
            given(folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(OWNER_ID))
                .willReturn(List.of(pub));
            given(itemRepository.findAllByFolderIdsOrderByRecentSemester(anyList()))
                .willReturn(List.of(item(10L, pub, null, 20261), item(11L, pub, Visibility.PRIVATE, 20261)));

            // when
            List<MaterialFolderResponse> result = materialService.getPublicFolders(OWNER_ID);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).itemCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getPublicItems 메서드")
    class GetPublicItemsTest {

        @Test
        @DisplayName("비공개 폴더 안의 개별 공개 자료는 노출하지 않는다 (폴더 비공개 우선)")
        void hidesItemInPrivateFolder_evenIfItemIsPublic() {
            // given
            MaterialFolder priv = folder(1L, null, Visibility.PRIVATE);
            given(folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(OWNER_ID))
                .willReturn(List.of(priv));

            // when
            List<MaterialItemResponse> result = materialService.getPublicItems(OWNER_ID, 0);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("공개 폴더 안의 개별 비공개 자료는 숨긴다")
        void hidesPrivateItem_inPublicFolder() {
            // given
            MaterialFolder pub = folder(1L, null, Visibility.PUBLIC);
            given(folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(OWNER_ID))
                .willReturn(List.of(pub));
            given(itemRepository.findAllByFolderIdsOrderByRecentSemester(anyList()))
                .willReturn(List.of(item(10L, pub, null, 20261), item(11L, pub, Visibility.PRIVATE, 20261)));

            // when
            List<MaterialItemResponse> result = materialService.getPublicItems(OWNER_ID, 0);

            // then
            assertThat(result).extracting(MaterialItemResponse::id).containsExactly(10L);
        }

        @Test
        @DisplayName("공개 응답에는 S3 원본 URL을 담지 않고 hasFile 로만 알린다")
        void masksFileUrl_inPublicResponse() {
            // given
            MaterialFolder pub = folder(1L, null, Visibility.PUBLIC);
            given(folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(OWNER_ID))
                .willReturn(List.of(pub));
            given(itemRepository.findAllByFolderIdsOrderByRecentSemester(anyList()))
                .willReturn(List.of(item(10L, pub, Visibility.PUBLIC, 20261)));

            // when
            MaterialItemResponse result = materialService.getPublicItems(OWNER_ID, 0).get(0);

            // then
            assertThat(result.fileUrl()).isNull();
            assertThat(result.hasFile()).isTrue();
        }

        @Test
        @DisplayName("limit 을 지정하면 그 개수만 반환한다")
        void appliesLimit() {
            // given
            MaterialFolder pub = folder(1L, null, Visibility.PUBLIC);
            given(folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(OWNER_ID))
                .willReturn(List.of(pub));
            given(itemRepository.findAllByFolderIdsOrderByRecentSemester(anyList()))
                .willReturn(List.of(
                    item(10L, pub, null, 20262),
                    item(11L, pub, null, 20261),
                    item(12L, pub, null, 20252)
                ));

            // when
            List<MaterialItemResponse> result = materialService.getPublicItems(OWNER_ID, 2);

            // then: 저장소가 최근 학기순으로 내려준 순서를 그대로 유지한다
            assertThat(result).extracting(MaterialItemResponse::id).containsExactly(10L, 11L);
        }
    }

    @Nested
    @DisplayName("getPublicItemDownloadUrl 메서드")
    class PublicDownloadTest {

        @Test
        @DisplayName("비공개 자료 ID로 직접 호출하면 404로 막는다")
        void throwsNotFound_forPrivateItem() {
            // given
            MaterialFolder pub = folder(1L, null, Visibility.PUBLIC);
            MaterialItem privateItem = item(10L, pub, Visibility.PRIVATE, 20261);
            given(itemRepository.findByIdAndUserId(10L, OWNER_ID)).willReturn(Optional.of(privateItem));
            given(folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(OWNER_ID))
                .willReturn(List.of(pub));

            // when & then
            assertThatThrownBy(() -> materialService.getPublicItemDownloadUrl(OWNER_ID, 10L))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("공개 자료는 presigned URL 을 발급한다")
        void issuesPresignedUrl_forPublicItem() {
            // given
            MaterialFolder pub = folder(1L, null, Visibility.PUBLIC);
            MaterialItem publicItem = item(10L, pub, null, 20261);
            given(itemRepository.findByIdAndUserId(10L, OWNER_ID)).willReturn(Optional.of(publicItem));
            given(folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(OWNER_ID))
                .willReturn(List.of(pub));
            given(s3StorageClient.extractKeyFromUrl(publicItem.getFileUrl())).willReturn("material/10.pdf");
            given(s3StorageClient.getPresignedDownloadUrl("material/10.pdf", "10.pdf"))
                .willReturn("https://signed.example.com/material/10.pdf");

            // when
            String url = materialService.getPublicItemDownloadUrl(OWNER_ID, 10L).downloadUrl();

            // then
            assertThat(url).isEqualTo("https://signed.example.com/material/10.pdf");
        }
    }

    @Nested
    @DisplayName("getPublicFolderItems 메서드")
    class GetPublicFolderItemsTest {

        @Test
        @DisplayName("상위 폴더가 비공개면 하위 폴더 자료 조회를 404로 막는다")
        void throwsNotFound_whenAncestorIsPrivate() {
            // given
            MaterialFolder parent = folder(1L, null, Visibility.PRIVATE);
            MaterialFolder child = folder(2L, parent, Visibility.PUBLIC);
            given(folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(OWNER_ID))
                .willReturn(List.of(parent, child));

            // when & then
            assertThatThrownBy(() -> materialService.getPublicFolderItems(OWNER_ID, 2L))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.NOT_FOUND.getMessage());
        }
    }
}
