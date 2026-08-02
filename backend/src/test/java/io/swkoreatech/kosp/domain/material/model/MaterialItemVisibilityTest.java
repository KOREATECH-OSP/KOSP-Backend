package io.swkoreatech.kosp.domain.material.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 학습자료의 실제 공개 여부 판정 테스트.
 *
 * <p>핵심 규칙: 폴더가 비공개면 아이템을 PUBLIC 으로 지정해도 노출되지 않는다.
 * 공개 조회 API 가 공개 폴더로 먼저 걸러내므로, 엔티티의 판정도 같은 기준이어야
 * "공개로 표시되는데 남에게는 안 보이는" 불일치가 생기지 않는다.</p>
 */
class MaterialItemVisibilityTest {

    private MaterialFolder folder(Visibility visibility) {
        MaterialFolder folder = MaterialFolder.builder()
            .name("폴더")
            .folderType(FolderType.CUSTOM)
            .visibility(visibility)
            .build();
        ReflectionTestUtils.setField(folder, "id", 1L);
        return folder;
    }

    private MaterialItem itemIn(MaterialFolder folder, Visibility itemVisibility) {
        MaterialItem item = MaterialItem.builder()
            .title("자료")
            .source(MaterialSource.MANUAL)
            .visibility(itemVisibility)
            .build();
        ReflectionTestUtils.setField(item, "folder", folder);
        return item;
    }

    @Test
    @DisplayName("비공개 폴더 안의 자료는 PUBLIC 으로 지정해도 공개되지 않는다")
    void publicItemInPrivateFolderIsNotPublic() {
        MaterialItem item = itemIn(folder(Visibility.PRIVATE), Visibility.PUBLIC);

        assertThat(item.isPublic()).isFalse();
        // 다만 사용자가 공개로 "지정"한 상태라는 것은 구분해서 안내할 수 있어야 한다
        assertThat(item.isMarkedPublic()).isTrue();
        assertThat(item.isFolderPublic()).isFalse();
    }

    @Test
    @DisplayName("공개 폴더 안에서 PUBLIC 으로 지정한 자료는 공개된다")
    void publicItemInPublicFolderIsPublic() {
        MaterialItem item = itemIn(folder(Visibility.PUBLIC), Visibility.PUBLIC);

        assertThat(item.isPublic()).isTrue();
    }

    @Test
    @DisplayName("공개 폴더 안에서 override 가 없으면 폴더 설정을 상속해 공개된다")
    void itemWithoutOverrideInheritsPublicFolder() {
        MaterialItem item = itemIn(folder(Visibility.PUBLIC), null);

        assertThat(item.isPublic()).isTrue();
    }

    @Test
    @DisplayName("공개 폴더 안이라도 PRIVATE 으로 지정한 자료는 공개되지 않는다")
    void privateItemInPublicFolderIsNotPublic() {
        MaterialItem item = itemIn(folder(Visibility.PUBLIC), Visibility.PRIVATE);

        assertThat(item.isPublic()).isFalse();
    }

    @Test
    @DisplayName("비공개 폴더 안에서 override 가 없으면 비공개다 (기본값 비공개)")
    void itemWithoutOverrideInheritsPrivateFolder() {
        MaterialItem item = itemIn(folder(Visibility.PRIVATE), null);

        assertThat(item.isPublic()).isFalse();
    }
}
