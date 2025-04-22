package org.shax3.square.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {
    INVALID_REQUEST(1000, "유효하지 않은 요청입니다."),
    NOT_FOUND(1001, "자원을 찾을 수가 없습니다."),
    UNSUPPORTED_EXTENSIONS(1002, "지원하지 않는 파일 형식입니다."
            + " 지원하는 파일 양식(\"jpg\", \"png\", \"gif\", \"jpeg\", \"webp\""),
    DUPLICATED_SOURCE(1003, "입력하는 요소에 중복이 존재합니다. 요소의 명칭을 변경해주세요."),
    ALREADY_DELETED(1004, "이미 삭제된 항목입니다."),
    INVALID_TARGET_TYPE(1005, "유효하지 않은 타겟 타입입니다."),
    INVALID_ID_TOKEN(1007, "유효하지 않은 ID_TOKEN 입니다."),
    FIREBASE_INITIALIZE_ERROR(1008, "Firebase 가 초기화하지 못했습니다."),
    FIREBASE_MESSAGE_ERROR(1009, "FCM에 문제가 생겼습니다."),
    UNABLE_TO_GET_USER_INFO(2001, "소셜 로그인 공급자로부터 유저 정보를 받아올 수 없습니다."),
    UNABLE_TO_GET_ACCESS_TOKEN(2002, "소셜 로그인 공급자로부터 인증 토큰을 받아올 수 없습니다."),
    NICKNAME_BLANK_NOT_ALLOW(2003, "닉네임 및 이름에는 빈칸을 허용하지 않습니다."),
    AGE_LIMIT_FROM_TEN(2004, "10대 미만의 사용자는 서비스 이용이 불가합니다."),
    SOCIAL_TYPE_MISMATCH(2005, "이미 다른 소셜 계정이 존재합니다."),
    SIGN_UP_TOKEN_INVALID(2006, "회원가입 페이지가 만료되었습니다. 다시 시도해주세요."),
    DUPLICATE_EMAIL(2007, "해당 이메일로 회원가입한 계정이 이미 존재합니다."),
    NOT_PROFILE_IMG(2008, "사진이 유효하지 않습니다."),
    USER_DELETED(2009, "탈퇴한 회원입니다."),
    USER_INACTIVE(2010, "비활성 회원입니다."),
    USER_NOT_FOUND(2011, "유저를 찾을 수 없습니다."),
    USER_TYPE_NOT_FOUND(2012, "타입이 없는 유저입니다."),
    USER_NOT_AUTHORIZED(2013, "해당 유저는 권한이 없습니다."),

    UNAUTHORIZED_ACCESS(3000, "접근할 수 없는 리소스입니다."),
    INVALID_REFRESH_TOKEN(3001, "사용자 식별에 실패했습니다. 다시 로그인해주세요."),
    FAILED_TO_VALIDATE_TOKEN(3002, "토큰 검증에 실패했습니다."),
    INVALID_ACCESS_TOKEN(3003, "사용자 식별에 실패했습니다. 다시 로그인해주세요."),

    NOT_AUTHOR(4001, "작성자만 수정할 수 있습니다."),
    PAGE_LIMIT(4002, "최대 페이지 수를 초과할 수 없습니다."),
    POST_LIMIT(4003, "게시판의 최대 게시글 수를 초과했습니다."),
    POST_NOT_FOUND(4004, "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(4005, "댓글을 찾을 수 없습니다."),
    POST_IMAGE_LIMIT(4006, "하나의 게시글에는 최대 3개의 이미지를 업로드할 수 있습니다."),
    IMAGE_NOT_FOUND(4007, "이미지 삭제가 정상적으로 처리되지 않았습니다."),
    DUPLICATE_IMAGE(4008, "같은 s3Key는 업로드할 수 없습니다"),
    INVALID_S3_KEY(4009, "유효하지 않은 s3Key입니다."),

    PROPOSAL_NOT_FOUND(5004, "청원을 찾을 수 없습니다."),

    DEBATE_NOT_FOUND(6001, "토론을 찾을 수 없습니다."),

    ALREADY_VOTED(7001, "이미 투표한 토론입니다."),

    OPINION_NOT_FOUND(8001, "해당 의견을 찾을 수 없습니다."),
    OPINION_DELETED(8002, "이미 삭제된 의견입니다."),

    OPINION_COMMENT_NOT_FOUND(9001, "해당 의견 답글을 찾을 수 없습니다."),

    SCRAP_ALREADY_EXISTS(10001, "이미 해당 게시물/논쟁을 스크랩하고 있습니다."),

    TYPE_QUESTION_DUPLICATION(11002, "같은 질문에 중복 답변이 존재합니다."),
    TYPE_RESULT_NOT_FOUND(11003, "성향테스트 결과가 존재하지 않습니다."),

    INVALID_PARENT_COMMENT(12001, "유효하지 않은 부모 댓글입니다."),

    NOTIFICATION_NOT_FOUND(13001, "해당 알림이 존재하지 않습니다.");
    private final int code;
    private final String message;
}

