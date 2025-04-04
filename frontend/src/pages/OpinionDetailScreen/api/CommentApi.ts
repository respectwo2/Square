import { axiosInstance } from "../../../shared";
import { Comment } from "../Components/Comment.types";
import { OpinionsResponse } from "../Components/OpinionsResponse.types";


export const getOpinionDetail = async (
    commentId: number,
): Promise<OpinionsResponse> => {
    try {
        const response = await axiosInstance.get(`/api/opinions/${commentId}`);
        return response.data;
    } catch (error) {
        console.error("의견 상세 조회 실패:", error);
        throw error;
    }
}

export const createComment = async (
    commentId: number,
    content: String,
): Promise<Comment> => {
    try {
        const response = await axiosInstance.post('/api/opinions/comments',
            {
                commentId: commentId,
                content: content,
            }
        );
        return response.data;
    } catch (error) {
        console.error("의견 생성 실패:", error);
        throw error;
    }
};

export const updateComment = async (
    commentId: number,
    content: String,
): Promise<Comment> => {
    try {
        const response = await axiosInstance.put(`/api/opinions/comments/${commentId}`,
            {
                commentId: commentId,
                content: content,
            }
        );
        return response.data;
    } catch (error) {
        console.error("의견 업데이트 실패:", error);
        throw error;
    }
};

export const deleteComment = async (
    commentId: number,
) => {
    try {
        const response = await axiosInstance.delete(`/api/opinions/comments/${commentId}`);
        return response.data;
    } catch (error) {
        console.error("의견 삭제 실패:", error);
        throw error;
    }
};