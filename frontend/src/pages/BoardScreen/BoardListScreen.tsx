import React, { useCallback, useEffect, useState } from "react";
import {
  View,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Alert,
  ActivityIndicator,
  Platform,
  SafeAreaView,
} from "react-native";
import { StackNavigationProp } from "@react-navigation/stack";
import { RouteProp, useFocusEffect } from "@react-navigation/native";
import { BoardAPI } from "../BoardScreen/Api/boardApi"; // 게시판 API 호출
import BoardItem from "./components/BoardItem"; // 개별 게시글 항목을 표시하는 컴포넌트
import EmptyBoardList from "./components/EmptyBoardList"; // 게시글이 없을 때 표시하는 컴포넌트
import { Icons } from "../../../assets/icons/Icons";
import { BoardStackParamList } from "../../shared/page-stack/BoardPageStack";
import Text from '../../components/Common/Text';
import colors from "../../../assets/colors";

// 인기 게시글 인터페이스
interface PopularPost {
  postId: number;
  title: string;
  createdAt: string;
  likeCount: number;
  commentCount: number;
}
// 일반 게시글 인터페이스
interface Post {
  postId: number;
  nickname: string;
  profileUrl: string;
  userType: string;
  createdAt: string;
  title: string;
  content: string;
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
}
// API 응답 인터페이스
interface PostsResponse {
  userType: string | null;
  popular: PopularPost[];
  posts: Post[];
  nextCursorId: number | null;
  nextCursorLikes: number | null;
}

// 네비게이션 프롭 타입 정의
type BoardListScreenNavigationProp = StackNavigationProp<
  BoardStackParamList,
  "BoardList"
>;

// props 타입 정의
interface BoardListScreenProps {
  navigation: BoardListScreenNavigationProp;
  route: RouteProp<BoardStackParamList, "BoardList">;
}

export default function BoardListScreen({
  route,
  navigation,
}: BoardListScreenProps) {
  const [boards, setBoards] = useState<Post[]>([]); // 게시글 목록 데이터 상태 관리
  const [popularPosts, setPopularPosts] = useState<PopularPost[]>([]); // 인기 게시글 목록 데이터 상태 관리
  const [loading, setLoading] = useState(false); // 데이터 로딩 상태 관리
  const [nextCursorId, setNextCursorId] = useState<number | null>(null);
  const [nextCursorLikes, setNextCursorLikes] = useState<number | null>(null);
  const [sort, setSort] = useState<"latest" | "likes">("latest"); // 정렬 방식

  // 초기 데이터 로딩
  useEffect(() => {
    fetchBoards(true); // 컴포넌트 마운트 시 데이터 가져오기
  }, []); // 빈 의존성 배열은 컴포넌트가 처음 마운트 될 때만 실행됨

  // 데이터 변경이 감지되면 데이터 다시 로드 (게시글 실시간 업데이트)
  useEffect(() => {
    if (route.params?.refresh) {
      fetchBoards(true); // 새로고침 수행
      navigation.setParams({ refresh: undefined }); // 플래그 초기화
    }
  }, [route.params?.refresh]); // 정렬 방식이 변경되면 데이터 다시 로드

  // 화면에 포커스가 올 때마다 데이터 새로고침 (댓글 개수 실시간 업데이트)
  useFocusEffect(
    useCallback(() => {
      fetchBoards(true); // 전체 데이터 새로고침
      return () => {
        // 정리 작업 (필요한 경우)
      };
    }, [sort]) // sort가 변경되면 다시 실행
  );

  // 게시글 목록을 서버에서 가져오는 함수
  const fetchBoards = async (refresh = false) => {
    try {
      setLoading(true); // 로딩 상태를 true로 설정
      // 새로고침 시 커서 초기화
      const cursor = refresh ? null : nextCursorId;
      const response = await BoardAPI.getPosts(
        sort,
        cursor,
        sort === "likes" ? nextCursorLikes : null,
        10 // limit
      );

      const data = response.data as PostsResponse;

      // 새로고침 또는 첫 로드 시 전체 데이터 설정, 그렇지 않으면 중복 제거 후 추가
      if (refresh) {
        setBoards(data.posts);
        setPopularPosts(data.popular);
      } else {
        const uniquePosts = data.posts.filter(
          (newPost) =>
            !boards.some(
              (existingPost) => existingPost.postId === newPost.postId
            )
        );
        setBoards((prev) => [...prev, ...uniquePosts]);
      }

      setNextCursorId(data.nextCursorId);
      setNextCursorLikes(data.nextCursorLikes);
    } catch (error) {
      console.error("게시글 목록을 불러오는 데 실패했습니다:", error);
      Alert.alert("오류", "게시글 목록을 불러오는 데 실패했습니다.");
    } finally {
      setLoading(false); // 로딩 상태를 false로 설정
    }
  };

  const handleSortChange = (newSort: "latest" | "likes") => {
    setSort(newSort);
  };

  return loading && boards.length === 0 ? (
    <View style={styles.loadingContainer}>
      <ActivityIndicator size="large" color="#007BFF" />
    </View>
  ) : (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container}>
        {/* 인기 게시글 (캐러셀) */}
        {popularPosts.length > 0 && (
          <View style={styles.popularSection}>
            <Text style={styles.sectionTitle}>HOT</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false}>
              {popularPosts.map((post) => (
                <TouchableOpacity
                  key={post.postId}
                  onPress={() =>
                    navigation.navigate("BoardDetail", { boardId: post.postId })
                  }
                >
                  <View style={styles.popularPostItem}>
                    <Text numberOfLines={1} style={styles.popularPostTitle}>
                      {post.title}
                    </Text>
                    <Text style={styles.popularPostStats}>
                      좋아요 {post.likeCount} • 댓글 {post.commentCount}
                    </Text>
                  </View>
                </TouchableOpacity>
              ))}
            </ScrollView>
          </View>
        )}

        {/* 정렬 옵션 */}
        <View style={styles.sortOptions}>
          <TouchableOpacity
            style={[
              styles.sortButton,
              sort === "latest" && styles.activeSortButton,
            ]}
            onPress={() => handleSortChange("latest")}
          >
            <Text
              style={
                sort === "latest" ? styles.activeSortText : styles.sortText
              }
            >
              최신순
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[
              styles.sortButton,
              sort === "likes" && styles.activeSortButton,
            ]}
            onPress={() => handleSortChange("likes")}
          >
            <Text
              style={sort === "likes" ? styles.activeSortText : styles.sortText}
            >
              인기순
            </Text>
          </TouchableOpacity>
        </View>

        {/* 게시글 목록 */}
        <FlatList
          data={boards}
          renderItem={({ item }) => (
            <BoardItem
              item={item}
              onPress={() =>
                navigation.navigate("BoardDetail", { boardId: item.postId })
              }
            />
          )}
          keyExtractor={(item) => item.postId.toString()}
          onEndReached={() => {
            if (nextCursorId || nextCursorLikes) {
              fetchBoards();
            }
          }}
          onEndReachedThreshold={0.1}
          contentContainerStyle={[
            styles.listContent,
            boards.length === 0 && styles.emptyListContent,
          ]} // 빈 상태면 세로축 중앙 정렬
          // 빈 상태 컴포넌트 추가
          ListEmptyComponent={
            <EmptyBoardList
              onPressWrite={() =>
                navigation.navigate("BoardWrite", { postId: undefined })
              }
            />
          }
        />

        {/* 글쓰기 버튼 */}
        <TouchableOpacity
          style={styles.writeButton}
          onPress={() =>
            navigation.navigate("BoardWrite", { postId: undefined })
          }
        >
          <Icons.write />
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: "#fff",
  },
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  listContent: {
    paddingBottom: 90, // 하단 네비게이션 바 높이(60) + 여백(30)
  },
  emptyListContent: {
    flex: 1, // 컨텐츠가 없을 때 전체 공간 사용
    justifyContent: "center",
    alignItems: "center",
  },
  popularSection: {
    padding: 20,
    borderBottomWidth: 1,
    borderBottomColor: "#eee",
  },
  sectionTitle: {
    fontSize: 18,
    marginBottom: 14,
    color: colors.warnRed
  },
  popularPostItem: {
    width: 160,
    marginRight: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: "#eee",
    borderRadius: 8,
    backgroundColor: colors.background
  },
  popularPostTitle: {
    fontSize: 13,
    fontWeight: "bold",
    marginBottom: 8,
  },
  popularPostStats: {
    fontSize: 11,
    color: colors.grayText,
  },
  sortOptions: {
    flexDirection: "row",
    padding: 18,
  },
  sortButton: {
    marginRight: 18,
    paddingVertical: 5,
  },
  activeSortButton: {
    borderBottomWidth: 2,
    borderBottomColor: "#007BFF",
  },
  sortText: {
    color: colors.grayText,
  },
  activeSortText: {
    color: "#007BFF",
    fontWeight: "bold",
  },
  writeButton: {
    position: "absolute",
    right: 20,
    bottom: Platform.OS === "ios" ? 100 : 90, // 하단 네비게이션 바 위에 배치
    backgroundColor: colors.yesDark,
    width: 60,
    height: 60,
    borderRadius: 30,
    justifyContent: "center",
    alignItems: "center",
    elevation: 5,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    zIndex: 1, // 네비게이션 바보다 위에 표시되도록 설정
  },
  writeButtonText: {
    color: "white",
    fontSize: 16,
    fontWeight: "bold",
  },
  loadingContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#fff",
  },
});
