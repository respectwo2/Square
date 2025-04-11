import { Post, Reply } from "../board.types";

// 현재 사용자 정보
export const currentUser = {
  id: 1,
  nickname: "반짝이는하마",
  profileUrl: "",
  userType: "PNTB",
};

// 게시글 목 데이터
export const mockPosts: Post[] = [
  // 사용자 본인이 작성한 글
  {
    postId: 1,
    nickname: currentUser.nickname,
    profileUrl: currentUser.profileUrl,
    userType: currentUser.userType,
    createdAt: "2025-03-25T12:34:56Z",
    title: "내가 작성한 게시글입니다",
    content: "게시판 테스트를 위한 글입니다. 목 데이터로 구현하였습니다.",
    likeCount: 15,
    commentCount: 3,
    isLiked: false,
    isScrapped: false,
    comments: [
      {
        commentId: 101,
        parentId: undefined, // 최상위 댓글
        nickname: "반짝이는코알라",
        profileUrl: "",
        userType: "PNTB",
        createdAt: "2025-03-25T13:00:00Z",
        content: "첫 번째 댓글입니다",
        likeCount: 5,
        isLiked: true,
        replyCount: 5, // 실제 총 대댓글 개수
        // 초기 로드 시 보여줄 대댓글 (예시: 3개만)
        replies: [
          {
            commentId: 201,
            parentId: 101,
            nickname: "즐거운팬더",
            profileUrl: "",
            userType: "PNTB",
            createdAt: "2025-03-25T13:15:00Z",
            content: "첫 번째 대댓글입니다",
            likeCount: 3,
            isLiked: false,
          },
          {
            commentId: 202,
            parentId: 101,
            nickname: currentUser.nickname,
            profileUrl: currentUser.profileUrl,
            userType: currentUser.userType,
            createdAt: "2025-03-25T13:20:00Z",
            content: "내가 작성한 대댓글입니다",
            likeCount: 2,
            isLiked: true,
          },
          {
            commentId: 304,
            parentId: 101,
            nickname: "게으른나무늘보",
            profileUrl: "",
            userType: "ICTR",
            createdAt: "2025-03-25T13:25:00Z",
            content: "저도 동의해요!",
            likeCount: 0,
            isLiked: false,
          },
        ] as Reply[] | undefined,
      },
      {
        commentId: 102,
        parentId: undefined,
        nickname: currentUser.nickname,
        profileUrl: currentUser.profileUrl,
        userType: currentUser.userType,
        createdAt: "2025-03-25T14:00:00Z",
        content: "내가 작성한 두 번째 댓글입니다",
        likeCount: 4,
        isLiked: false,
        replyCount: 1,
        replies: [
          {
            commentId: 203,
            parentId: 102,
            nickname: "똑똑한사자",
            profileUrl: "",
            userType: "PNTB",
            createdAt: "2025-03-25T14:15:00Z",
            content: "대댓글 테스트입니다",
            likeCount: 1,
            isLiked: false,
          },
        ] as Reply[] | undefined,
      },
      {
        commentId: 103,
        parentId: undefined,
        nickname: "행복한기린",
        profileUrl: "",
        userType: "PNTB",
        createdAt: "2025-03-25T15:00:00Z",
        content: "세 번째 댓글입니다",
        likeCount: 2,
        isLiked: false,
        replyCount: 0,
        replies: [] as Reply[] | undefined,
      },
    ],
  },

  // 다른 사용자가 작성한 글 1
  {
    postId: 2,
    nickname: "반짝이는코알라",
    profileUrl: "",
    userType: "PNTB",
    createdAt: "2025-03-24T10:34:56Z",
    title: "안녕하세요! 반가워요",
    content: "게시판에 글을 작성해봅니다. 모두 반갑습니다. 좋은 하루 되세요!",
    likeCount: 23,
    commentCount: 3,
    isLiked: true,
    isScrapped: true,
    comments: [
      {
        commentId: 104,
        parentId: undefined,
        nickname: currentUser.nickname,
        profileUrl: currentUser.profileUrl,
        userType: currentUser.userType,
        createdAt: "2025-03-24T11:00:00Z",
        content: "저도 반가워요! 좋은 글 감사합니다.",
        likeCount: 7,
        isLiked: false,
        replyCount: 2,
        replies: [
          {
            commentId: 204,
            parentId: 104,
            nickname: "반짝이는코알라",
            profileUrl: "",
            userType: "PNTB",
            createdAt: "2025-03-24T11:15:00Z",
            content: "댓글 남겨주셔서 감사합니다!",
            likeCount: 3,
            isLiked: true,
          },
          {
            commentId: 205,
            parentId: 104,
            nickname: "즐거운팬더",
            profileUrl: "",
            userType: "PNTB",
            createdAt: "2025-03-24T11:30:00Z",
            content: "모두 좋은 하루 되세요~",
            likeCount: 2,
            isLiked: false,
          },
        ] as Reply[] | undefined,
      },
      {
        commentId: 105,
        parentId: undefined,
        nickname: "똑똑한사자",
        profileUrl: "",
        userType: "PNTB",
        createdAt: "2025-03-24T12:00:00Z",
        content: "글 잘 봤습니다!",
        likeCount: 3,
        isLiked: false,
        replyCount: 1,
        replies: [
          {
            commentId: 206,
            parentId: 105,
            nickname: "반짝이는코알라",
            profileUrl: "",
            userType: "PNTB",
            createdAt: "2025-03-24T12:15:00Z",
            content: "감사합니다 :)",
            likeCount: 1,
            isLiked: false,
          },
        ] as Reply[] | undefined,
      },
      {
        commentId: 106,
        parentId: undefined,
        nickname: "행복한기린",
        profileUrl: "",
        userType: "PNTB",
        createdAt: "2025-03-24T13:00:00Z",
        content: "좋은 글이네요!",
        likeCount: 4,
        isLiked: true,
        replyCount: 0,
        replies: [] as Reply[] | undefined,
      },
    ],
  },

  // 다른 사용자가 작성한 글 2
  {
    postId: 3,
    nickname: "즐거운팬더",
    profileUrl: "",
    userType: "PNTB",
    createdAt: "2025-03-23T09:34:56Z",
    title: "오늘의 날씨가 정말 좋네요",
    content:
      "오늘 날씨가 정말 좋습니다. 여러분 모두 나들이 어떠신가요? 좋은 시간 보내세요!",
    likeCount: 45,
    commentCount: 3,
    isLiked: false,
    isScrapped: false,
    comments: [
      {
        commentId: 107,
        parentId: undefined,
        nickname: "행복한기린",
        profileUrl: "",
        userType: "PNTB",
        createdAt: "2025-03-23T10:00:00Z",
        content: "정말 날씨가 좋네요! 저도 나들이 가려구요.",
        likeCount: 6,
        isLiked: false,
        replyCount: 2,
        replies: [
          {
            commentId: 207,
            parentId: 107,
            nickname: "즐거운팬더",
            profileUrl: "",
            userType: "PNTB",
            createdAt: "2025-03-23T10:15:00Z",
            content: "좋은 시간 보내세요~",
            likeCount: 2,
            isLiked: false,
          },
          {
            commentId: 208,
            parentId: 107,
            nickname: currentUser.nickname,
            profileUrl: currentUser.profileUrl,
            userType: currentUser.userType,
            createdAt: "2025-03-23T10:30:00Z",
            content: "저도 같이 가고 싶네요!",
            likeCount: 3,
            isLiked: true,
          },
        ] as Reply[] | undefined,
      },
      {
        commentId: 108,
        parentId: undefined,
        nickname: "반짝이는코알라",
        profileUrl: "",
        userType: "PNTB",
        createdAt: "2025-03-23T11:00:00Z",
        content: "날씨 정보 감사합니다!",
        likeCount: 4,
        isLiked: false,
        replyCount: 1,
        replies: [
          {
            commentId: 209,
            parentId: 108,
            nickname: "즐거운팬더",
            profileUrl: "",
            userType: "PNTB",
            createdAt: "2025-03-23T11:15:00Z",
            content: "천만에요~",
            likeCount: 1,
            isLiked: false,
          },
        ] as Reply[] | undefined,
      },
      {
        commentId: 109,
        parentId: undefined,
        nickname: currentUser.nickname,
        profileUrl: currentUser.profileUrl,
        userType: currentUser.userType,
        createdAt: "2025-03-23T12:00:00Z",
        content: "좋은 정보 감사합니다!",
        likeCount: 5,
        isLiked: false,
        replyCount: 0,
        replies: [] as Reply[] | undefined,
      },
    ],
  },
];

// 인기 게시글 목 데이터
export const mockPopularPosts = [
  {
    postId: 3,
    title: "오늘의 날씨가 정말 좋네요",
    createdAt: "2025-03-23T09:34:56Z",
    likeCount: 45,
    commentCount: 3,
  },
  {
    postId: 2,
    title: "안녕하세요! 반가워요",
    createdAt: "2025-03-24T10:34:56Z",
    likeCount: 23,
    commentCount: 3,
  },
  {
    postId: 1,
    title: "내가 작성한 게시글입니다",
    createdAt: "2025-03-25T12:34:56Z",
    likeCount: 15,
    commentCount: 3,
  },
];
// --- 대댓글 더보기용 전체 대댓글 데이터 ---
export const mockAllReplies: { [key: number]: Reply[] } = {
  // key: 부모 댓글의 commentId
  // value: 해당 부모 댓글에 달린 모든 대댓글 Reply 객체 배열
  101: [
    // commentId: 101 댓글의 모든 대댓글 (총 5개라고 가정)
    {
      commentId: 201,
      parentId: 101,
      nickname: "즐거운팬더",
      profileUrl: "",
      userType: "PNTB",
      createdAt: "2025-03-25T13:15:00Z",
      content: "첫 번째 대댓글입니다",
      likeCount: 3,
      isLiked: false,
    },
    {
      commentId: 202,
      parentId: 101,
      nickname: currentUser.nickname,
      profileUrl: currentUser.profileUrl,
      userType: currentUser.userType,
      createdAt: "2025-03-25T13:20:00Z",
      content: "내가 작성한 대댓글입니다",
      likeCount: 2,
      isLiked: true,
    },
    // --- 아래는 '더보기'로 로드될 대댓글들 ---
    {
      commentId: 304,
      parentId: 101,
      nickname: "게으른나무늘보",
      profileUrl: "",
      userType: "ICTR",
      createdAt: "2025-03-25T13:25:00Z",
      content: "저도 동의해요!",
      likeCount: 0,
      isLiked: false,
    },
    {
      commentId: 305,
      parentId: 101,
      nickname: "용감한호랑이",
      profileUrl: "https://picsum.photos/id/40/50/50",
      userType: "PNTB",
      createdAt: "2025-03-25T13:30:00Z",
      content: "...",
      likeCount: 1,
      isLiked: false,
    },
    {
      commentId: 306,
      parentId: 101,
      nickname: "신비로운부엉이",
      profileUrl: "",
      userType: "ICTR",
      createdAt: "2025-03-25T13:35:00Z",
      content: "흥미롭네요.",
      likeCount: 0,
      isLiked: false,
    },
  ],
  102: [
    // commentId: 102 댓글의 모든 대댓글 (총 1개)
    {
      commentId: 203,
      parentId: 102,
      nickname: "똑똑한사자",
      profileUrl: "",
      userType: "PNTB",
      createdAt: "2025-03-25T14:15:00Z",
      content: "대댓글 테스트입니다",
      likeCount: 1,
      isLiked: false,
    },
  ],
  103: [], // commentId: 103 댓글에는 대댓글 없음
  104: [
    {
      commentId: 204,
      parentId: 104,
      nickname: "반짝이는코알라",
      profileUrl: "",
      userType: "PNTB",
      createdAt: "2025-03-24T11:15:00Z",
      content: "댓글 남겨주셔서 감사합니다!",
      likeCount: 3,
      isLiked: true,
    },
    {
      commentId: 205,
      parentId: 104,
      nickname: "즐거운팬더",
      profileUrl: "",
      userType: "PNTB",
      createdAt: "2025-03-24T11:30:00Z",
      content: "모두 좋은 하루 되세요~",
      likeCount: 2,
      isLiked: false,
    },
  ],
  105: [
    {
      commentId: 206,
      parentId: 105,
      nickname: "반짝이는코알라",
      profileUrl: "",
      userType: "PNTB",
      createdAt: "2025-03-24T12:15:00Z",
      content: "감사합니다 :)",
      likeCount: 1,
      isLiked: false,
    },
  ],
  107: [
    {
      commentId: 207,
      parentId: 107,
      nickname: "즐거운팬더",
      profileUrl: "",
      userType: "PNTB",
      createdAt: "2025-03-23T10:15:00Z",
      content: "좋은 시간 보내세요~",
      likeCount: 2,
      isLiked: false,
    },
    {
      commentId: 208,
      parentId: 107,
      nickname: currentUser.nickname,
      profileUrl: currentUser.profileUrl,
      userType: currentUser.userType,
      createdAt: "2025-03-23T10:30:00Z",
      content: "저도 같이 가고 싶네요!",
      likeCount: 3,
      isLiked: true,
    },
  ],
  108: [
    {
      commentId: 209,
      parentId: 108,
      nickname: "즐거운팬더",
      profileUrl: "",
      userType: "PNTB",
      createdAt: "2025-03-23T11:15:00Z",
      content: "천만에요~",
      likeCount: 1,
      isLiked: false,
    },
  ],
  // ... 다른 부모 댓글 ID에 대한 대댓글 목록 ...
};

// 댓글 ID로 해당 댓글/대댓글 찾기 함수
export const findReplyById = (commentId: number): Reply | undefined => {
  // 1. mockAllReplies에서 먼저 찾기 (전체 대댓글 목록)
  for (const [parentId, replies] of Object.entries(mockAllReplies)) {
    const foundReply = replies.find((reply) => reply.commentId === commentId);
    if (foundReply) return foundReply;
  }

  // 2. mockPosts의 모든 댓글과 대댓글에서 찾기
  for (const post of mockPosts) {
    // 댓글 확인
    const foundComment = post.comments.find(
      (comment) => comment.commentId === commentId
    );
    if (foundComment) {
      // 댓글 자체를 Reply 타입으로 변환하여 반환 (필요한 속성만)
      return {
        commentId: foundComment.commentId,
        parentId: foundComment.parentId || 0,
        nickname: foundComment.nickname,
        profileUrl: foundComment.profileUrl,
        userType: foundComment.userType,
        createdAt: foundComment.createdAt,
        content: foundComment.content,
        likeCount: foundComment.likeCount,
        isLiked: foundComment.isLiked,
      };
    }

    // 각 댓글의 대댓글들 확인
    for (const comment of post.comments) {
      if (comment.replies && comment.replies.length > 0) {
        const foundReply = comment.replies.find(
          (reply) => reply.commentId === commentId
        );
        if (foundReply) return foundReply;
      }
    }
  }

  return undefined; // 찾지 못한 경우
};
