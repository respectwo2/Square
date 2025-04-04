import React, { useState, useCallback, useEffect } from 'react';
import { View, ActivityIndicator, FlatList, Dimensions, RefreshControl } from 'react-native';
import DebateCard from './DebateCard';
import { styles } from './DebateCard.styles';
import { getAllDebates } from '../api/DebateApi';
import { computeDebateListFields } from './Debate.types';
import { useDebateStore } from '../../../shared/stores/debates';
import { useAuthStore } from '../../../shared/stores/auth';

const { width, height } = Dimensions.get('window');

export default function DebateCardList() {
    const { loggedIn } = useAuthStore();
    const { debates, addDebates, clearDebates } = useDebateStore();
    const [nextCursorId, setNextCursorId] = useState<number | null>(null);
    const [loading, setLoading] = useState(false);
    const [hasMore, setHasMore] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const limit = 3;

    const fetchData = useCallback(async (refresh = false) => {
        if ((loading && !refresh) || (!hasMore && !refresh)) return;

        setLoading(true);

        try {
            // 리프레시 시에는 첫 페이지부터 다시 로드
            const cursorToUse = refresh ? null : nextCursorId;

            const response = await getAllDebates(cursorToUse, limit);
            const newData = response.debates;
            const nextId = response.nextCursorId;

            if (newData.length > 0) {
                // 통계 정보 계산해 넣기
                const computedDebates = computeDebateListFields(newData);

                // 리프레시 시에는 목록을 초기화하고 새 데이터만 표시
                if (refresh) {
                    clearDebates();
                    addDebates(computedDebates);
                    setHasMore(true);
                } else {
                    addDebates(computedDebates);
                }

                // 더이상 줄 데이터가 없다 알리면 페이징 막기 
                if (nextId == null) {
                    setHasMore(false);
                } else {
                    setNextCursorId(nextId);
                }
            } else {
                setHasMore(false);
            }
        } catch (error) {
            console.error("데이터 로드 실패:", error);
            setHasMore(false);
        }

        setLoading(false);
        setRefreshing(false);
    }, [nextCursorId, loading, hasMore, clearDebates, addDebates]);

    useEffect(() => {
        if (!loggedIn) {
            clearDebates();
        }
        fetchData();
    }, [loggedIn]);

    const loadMore = () => {
        if (!loading && hasMore) {
            fetchData();
        }
    };

    const onRefresh = useCallback(() => {
        setRefreshing(true);
        setNextCursorId(null);
        fetchData(true);
    }, [fetchData]);

    return (
        <View style={styles.CardListView}>
            <FlatList
                data={debates}
                renderItem={({ item }) => <DebateCard {...item} />}
                keyExtractor={(item) => item.debateId.toString()}
                onEndReached={loadMore}
                onEndReachedThreshold={0.5}
                ListFooterComponent={loading && !refreshing ? <ActivityIndicator size="large" /> : null}
                refreshControl={
                    <RefreshControl
                        refreshing={refreshing}
                        onRefresh={onRefresh}
                        colors={['#2196F3']}
                        tintColor={'#2196F3'}
                    />
                }
                pagingEnabled={true}
                snapToAlignment="center"
                snapToInterval={height}
                decelerationRate="fast"
                showsVerticalScrollIndicator={false}
            />
        </View>
    );
}