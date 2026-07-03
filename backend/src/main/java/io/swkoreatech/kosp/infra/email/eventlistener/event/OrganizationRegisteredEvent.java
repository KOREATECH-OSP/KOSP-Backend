package io.swkoreatech.kosp.infra.email.eventlistener.event;

/**
 * 조직 등록 이메일 발송 이벤트.
 *
 * <p>조직 Owner가 GitHub Organization을 K-OSP에 등록할 때 발행되며,
 * 이메일이 공개된 미가입 멤버에게 K-OSP 가입을 안내하는 메일을 발송한다.
 *
 * @param email          수신자 이메일 (GitHub 공개 이메일)
 * @param githubUsername 수신자 GitHub 아이디
 * @param ownerName      조직을 등록한 Owner 이름
 * @param orgName        등록된 조직 이름
 * @param clientUrl      클라이언트 기본 URL
 */
public record OrganizationRegisteredEvent(
    String email,
    String githubUsername,
    String ownerName,
    String orgName,
    String clientUrl
) {}
