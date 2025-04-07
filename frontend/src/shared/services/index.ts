/**
 * 서비스 레이어 모듈 모음
 * 모든 API 서비스를 한 곳에서 내보냅니다.
 */

// 게시판 관련 서비스
export { default as PostService } from "./postService";

// 댓글 관련 서비스
export { default as CommentService } from "./commentService";

// 이미지 업로드 관련 서비스
export { default as ImageService } from "./imageService";

// 좋아요 관련 서비스
export { LikeService, toggleLikeAPI } from "./likeService";

// 추가 서비스들은 여기에 추가
