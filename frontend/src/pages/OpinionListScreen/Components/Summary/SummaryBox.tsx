import React from "react";
import { View, Text, StyleSheet, Image } from "react-native";
import { Summary } from "./SummaryProps";
import { styles } from '../ContentBubble.styles';

import { AntDesign, Feather } from '@expo/vector-icons'; // 하트/댓글 아이콘용

interface Props {
    summary: Summary;
}

const SummaryBox = ({ summary }: Props) => {
    const {
        summaryId,
        content,
        isLeft,
    } = summary;

    return (
        <View
            style={[
                styles.bubbleWrapper,
                isLeft ? styles.alignLeft : styles.alignRight,
            ]}
        >
            <View
                style={[
                    styles.bubble,
                    isLeft ? styles.leftBubble : styles.rightBubble,
                ]}
            >
                {/* 본문 */}
                <Text style={styles.contentText}>{content}</Text>

            </View>
        </View>
    );
};

export default SummaryBox;
