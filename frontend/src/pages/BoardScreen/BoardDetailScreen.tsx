import React, { useCallback, useRef, useEffect } from "react";
import {
  View,
  StyleSheet,
  ScrollView,
  TextInput,
  TouchableOpacity,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator,
  Alert,
  UIManager,
  findNodeHandle,
} from "react-native";
import { StackScreenProps } from "@react-navigation/stack";
import ProfileImage from "../../components/ProfileImage/ProfileImage";
import CommentItem from "./components/CommentItem";
import { BoardStackParamList } from "../../shared/page-stack/BoardPageStack";
import { getTimeAgo } from "../../shared/utils/timeAge/timeAge";
import LikeButton from "../../components/LikeButton";
import { Icons } from "../../../assets/icons/Icons";
import PersonalityTag from "../../components/PersonalityTag/PersonalityTag";
import { usePostDetail, useComment } from "../../shared/hooks";
import { useLikeButton } from "../../shared/hooks/useLikeButton";
import Text from "../../components/Common/Text";
import colors from "../../../assets/colors";
import { useFocusEffect } from "@react-navigation/native";
import { Comment as BoardComment, Reply as BoardReply } from "./board.types";
import { Comment, Reply } from "../../shared/types/postTypes";

// 네비게이션 프롭 타입 정의
type Props = StackScreenProps<BoardStackParamList, "BoardDetail">;

// API 응답의 Reply를 내부 BoardReply 타입으로 변환하는 함수
const convertApiReplyToBoardReply = (apiReply: Reply): BoardReply => {
  return {
    replyId: apiReply.replyId,
    parentId: apiReply.parentId || 0, // board.types에서는 필수 필드이므로 기본값 제공
    nickname: apiReply.nickname,
    profileUrl: apiReply.profileUrl,
    userType: apiReply.userType || "", // board.types에서는 필수 필드이므로 기본값 제공
    createdAt: apiReply.createdAt,
    content: apiReply.content,
    likeCount: apiReply.likeCount,
    isLiked: apiReply.isLiked,
  };
};

// API 응답의 Comment를 내부 BoardComment 타입으로 변환하는 함수
const convertToComment = (apiComment: Comment): BoardComment => {
  return {
    commentId: apiComment.commentId,
    nickname: apiComment.nickname,
    profileUrl: apiComment.profileUrl,
    userType: apiComment.userType || "", // board.types에서는 필수 필드이므로 기본값 제공
    createdAt: apiComment.createdAt,
    content: apiComment.content,
    likeCount: apiComment.likeCount,
    isLiked: apiComment.isLiked,
    replyCount: apiComment.replyCount,
    // replies가 있으면 변환, 없으면 빈 배열
    replies: apiComment.replies
      ? apiComment.replies.map((reply) => convertApiReplyToBoardReply(reply))
      : [],
    isMe: false, // 백엔드가 제공하지 않으므로 기본값 설정
    updatedAt: apiComment.createdAt, // 수정 시각이 없으면 생성 시각 사용
  };
};

export default function BoardDetailScreen({ route, navigation }: Props) {
  // 라우트 파라미터에서 게시글 ID 가져오기
  const { boardId } = route.params;

  // *** ScrollView Ref 추가 ***
  const scrollViewRef = useRef<ScrollView>(null);

  // 게시글 상세 정보 훅
  const { post, loading, error, refresh } = usePostDetail(boardId);

  // 댓글 관련 훅
  const {
    commentText,
    setCommentText,
    submitting,
    createComment,
    submitError,
  } = useComment();

  // 댓글 아이템 refs를 저장할 객체
  const commentRefs = useRef<{ [key: number]: React.RefObject<View> }>({});

  /**
   * 특정 Y 좌표로 스크롤하는 함수
   * @param yPosition 스크롤할 Y 좌표
   */
  const scrollToY = useCallback((yPosition: number) => {
    if (scrollViewRef.current && typeof yPosition === "number") {
      console.log(`📜 Scrolling to Y: ${yPosition}`);
      scrollViewRef.current.scrollTo({ y: yPosition, animated: true });
    } else {
      console.warn(
        `⚠️ Invalid scroll position or ScrollView ref: ${yPosition}`
      );
    }
  }, []);

  /**
   * 특정 댓글로 스크롤하는 함수
   * @param commentId 포커싱할 댓글 ID
   */
  const scrollToComment = useCallback(
    (commentId: number) => {
      // 해당 ID의 댓글 ref가 있는지 확인
      const commentRef = commentRefs.current[commentId];

      if (commentRef && commentRef.current) {
        // measureInWindow를 사용하여 화면 상의 정확한 Y 좌표 측정
        commentRef.current.measureInWindow((x, y, width, height) => {
          // 상태바, 헤더 등의 오프셋 고려 (필요시 조정)
          const offsetY = 100;
          const scrollPosition = y - offsetY;

          console.log(
            `댓글 ${commentId} 위치 측정: Y=${y}, 계산된 스크롤 위치=${scrollPosition}`
          );
          scrollToY(scrollPosition);
        });
      } else {
        console.warn(`댓글 ID ${commentId}에 대한 ref를 찾을 수 없음`);
      }
    },
    [scrollToY]
  );

  // 화면에 포커스가 올 때마다 데이터 갱신 및 댓글 포커싱
  useFocusEffect(
    useCallback(() => {
      // 새로고침 로직 (기존 코드)
      if (route.params?.refresh) {
        refresh();
        navigation.setParams({ refresh: undefined });
      }

      // 댓글 포커싱 로직 수정
      const focusCommentId = route.params?.focusCommentId;
      if (focusCommentId && post) {
        // 포커싱할 댓글 찾기
        const targetComment = post.comments.find(
          (comment) => comment.commentId === focusCommentId
        );

        if (targetComment) {
          console.log(`포커싱할 댓글 찾음: ID ${focusCommentId}`);

          // 댓글 위치로 스크롤하기 위해 약간의 지연 시간을 두어 렌더링 완료 후 스크롤되도록 함
          setTimeout(() => {
            // 정확한 댓글 위치 측정 후 스크롤
            scrollToComment(focusCommentId);

            // 포커싱된 댓글 강조를 위한 파라미터는 유지 (CommentItem에서 타이머로 처리)
          }, 500);
        }
      }

      return () => {
        // 화면을 떠날 때 정리 작업 (필요한 경우)
      };
    }, [
      route.params?.refresh,
      route.params?.focusCommentId,
      refresh,
      navigation,
      post,
      scrollToComment,
    ])
  );

  // 댓글 작성 함수
  const handleSubmitComment = async () => {
    // 유효성 검사 및 API 호출은 useComment 훅에서 처리
    const success = await createComment(boardId); // parentId 없이 호출 (최상위 댓글)

    if (success) {
      // 댓글 작성 성공 시 입력창 비우고, 게시글 데이터 다시 가져오기
      // setCommentText(""); // 입력창 비우기는 useComment에서 처리됨
      refresh();
    }
  };

  // 게시글 좋아요 상태 변경 핸들러
  const handlePostLikeChange = useCallback(() => {
    // refresh 함수 호출로 데이터 새로고침
    refresh();
  }, [refresh]);

  // 로딩 중이면 로딩 인디케이터 표시
  if (loading && !post) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#007BFF" />
      </View>
    );
  }

  // 오류 발생 시
  if (error && !post) {
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorText}>게시글을 불러오는 데 실패했습니다.</Text>
        <Text style={styles.errorMessage}>{error.message}</Text>
        <TouchableOpacity style={styles.retryButton} onPress={refresh}>
          <Text style={styles.retryButtonText}>다시 시도</Text>
        </TouchableOpacity>
      </View>
    );
  }

  // post가 null일 때 처리
  if (!post) {
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorText}>게시글 정보를 찾을 수 없습니다.</Text>
        <TouchableOpacity
          style={styles.retryButton}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.retryButtonText}>뒤로 가기</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const likeProps = useLikeButton(
    post.postId,
    "POST",
    post.isLiked,
    post.likeCount,
    handlePostLikeChange
  );

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
      keyboardVerticalOffset={100} // 키보드가 올라올 때 화면 조정
    >
      {/* *** ScrollView에 ref 할당 *** */}
      <ScrollView style={styles.scrollView} ref={scrollViewRef}>
        {/* 게시글 헤더 (작성자 정보) */}
        <View style={styles.postHeader}>
          <ProfileImage imageUrl={post.profileUrl} variant="medium" />
          <View style={styles.authorInfo}>
            <View style={styles.nameContainer}>
              <Text style={styles.authorName}>{post.nickname}</Text>
              <PersonalityTag
                personality={post.userType || ""}
                nickname={post.nickname || ""}
              />
            </View>
            <Text style={styles.postDate}>{getTimeAgo(post.createdAt)}</Text>
          </View>
        </View>

        {/* 게시글 내용 */}
        <View style={styles.postContent}>
          <Text style={styles.postTitle}>{post?.title}</Text>
          <Text weight="medium" style={styles.postBody}>
            {post?.content}
          </Text>
        </View>

        {/* 댓글 영역 */}
        <View style={styles.commentsSection}>
          <View style={styles.commentsSectionHeader}>
            <View style={styles.interactionContainer}>
              <LikeButton {...likeProps} size="large" isVertical={false} />
              <View style={styles.commentCountContainer}>
                <Icons.commentNew />
                <Text weight="medium" style={styles.commentCountText}>
                  {post?.commentCount || 0}
                </Text>
              </View>
            </View>
          </View>

          {/* 댓글 목록 - CommentItem에 ref 전달하는 부분 수정 */}
          {post.comments && post.comments.length > 0 ? (
            post.comments.map((comment) => {
              // 각 댓글마다 ref 생성하여 저장
              if (!commentRefs.current[comment.commentId]) {
                commentRefs.current[comment.commentId] =
                  React.createRef<View>();
              }

              return (
                <CommentItem
                  key={comment.commentId}
                  comment={convertToComment(comment)}
                  postId={boardId}
                  onCommentChange={refresh}
                  onHideRepliesScrollRequest={scrollToY}
                  commentRef={commentRefs.current[comment.commentId]} // ref 전달
                />
              );
            })
          ) : (
            <View style={styles.emptyCommentsContainer}>
              <Text style={styles.emptyCommentsText}>
                아직 댓글이 없습니다. 첫 댓글을 남겨보세요!
              </Text>
            </View>
          )}
        </View>
      </ScrollView>

      {/* 댓글 입력 영역 */}
      <View style={styles.commentInputContainer}>
        <TextInput
          style={styles.commentInput}
          placeholder="댓글을 입력하세요..."
          value={commentText}
          onChangeText={setCommentText}
          multiline
        />
        <TouchableOpacity
          disabled={submitting || !commentText.trim()}
          style={[
            styles.commentSubmitButton,
            (submitting || !commentText.trim()) &&
              styles.commentSubmitButtonDisabled,
          ]}
          onPress={handleSubmitComment}
        >
          <Text style={styles.commentSubmitText}>
            {submitting ? "등록 중..." : "등록"}
          </Text>
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  scrollView: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#fff",
  },
  errorContainer: {
    flex: 1,
    padding: 18,
  },
  postHeader: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 5,
    marginTop: 20,
    marginLeft: 15,
  },
  authorInfo: {
    marginLeft: 15,
  },
  authorName: {
    fontSize: 16,
    fontWeight: "bold",
    marginRight: 6,
  },
  postDate: {
    fontSize: 12,
    color: colors.grayText,
    marginTop: 4,
  },
  postContent: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: "#eee",
    // marginBottom: 2,
    marginLeft: 10,
  },
  postTitle: {
    fontSize: 19,
    marginBottom: 8,
  },
  postBody: {
    fontSize: 15,
    lineHeight: 24,
  },
  commentsSection: {
    padding: 16,
  },
  commentsSectionHeader: {
    marginBottom: 20,
    borderBottomWidth: 1,
    borderBottomColor: "#eee",
  },
  commentsSectionTitle: {
    fontSize: 16,
    fontWeight: "bold",
  },
  interactionContainer: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 10,
  },
  commentCountContainer: {
    flexDirection: "row",
    alignItems: "center",
    marginLeft: 16,
  },
  commentCountText: {
    fontSize: 14,
    color: "gray",
    marginLeft: 4,
  },
  commentInputContainer: {
    flexDirection: "row",
    padding: 12,
    borderTopWidth: 1,
    borderTopColor: "#eee",
    backgroundColor: "#fff",
  },
  commentInput: {
    flex: 1,
    minHeight: 40,
    maxHeight: 80,
    borderWidth: 1,
    borderColor: "#ddd",
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 8,
    backgroundColor: "#f9f9f9",
  },
  commentSubmitButton: {
    marginLeft: 8,
    paddingHorizontal: 16,
    justifyContent: "center",
    backgroundColor: "#007BFF",
    borderRadius: 20,
  },
  commentSubmitButtonDisabled: {
    backgroundColor: "#ccc",
  },
  commentSubmitText: {
    color: "#fff",
    fontWeight: "bold",
  },
  nameContainer: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 4,
  },
  errorText: {
    fontSize: 16,
    color: "red",
    marginBottom: 10,
  },
  errorMessage: {
    fontSize: 14,
    color: "#666",
    textAlign: "center",
    marginBottom: 16,
  },
  retryButton: {
    backgroundColor: "#007BFF",
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderRadius: 5,
    marginTop: 10,
  },
  retryButtonText: {
    color: "white",
    fontWeight: "600",
  },
  emptyCommentsContainer: {
    padding: 16,
    alignItems: "center",
    justifyContent: "center",
  },
  emptyCommentsText: {
    fontSize: 14,
    color: "#666",
    textAlign: "center",
  },
});
