import * as React from 'react';
import {View, Text, StyleSheet} from 'react-native';

export default function MainScreen() {
    return (
        <View style={styles.container}>
            <Text>This Is Main Screen.</Text>
        </View>
    )
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    }
});