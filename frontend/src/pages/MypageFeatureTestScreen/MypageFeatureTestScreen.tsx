import { StyleSheet, Text, View, ScrollView, SafeAreaView } from "react-native"
import MypageButton from "../MypageScreen/Components/MypageButton";

import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNavigation } from '@react-navigation/native';

import {StackParamList} from '../../shared/page-stack/MyPageStack'

const MypageFeatureTestScreen = () => {
      const navigation = useNavigation<NativeStackNavigationProp<StackParamList>>();

    return (
        <View style={styles.container}>
            <MypageButton
              title="useAuth"
              onPress={() => navigation.navigate('UseAuthTestScreen')}
              variant="secondary"
              style={styles.actionButton}
            />
            <MypageButton
              title="API 테스트"
              onPress={() => navigation.navigate('SignupTestScreen')}
              variant="secondary"
              style={styles.actionButton}
            />
            <MypageButton
              title="ModalTestScreen"
              onPress={() => navigation.navigate('ModalTestScreen')}
              variant="secondary"
              style={styles.actionButton}
            />
            <MypageButton
              title="PersonalitySurveyPage"
              onPress={() => navigation.navigate('PersonalitySurveyPage')}
              variant="secondary"
              style={styles.actionButton}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    actionButton: {
      flex: 1,
      marginHorizontal: 4,
    },
    container: {
        flex: 1,
        backgroundColor: "#FFFFFF",
        paddingBottom: 40,
      },
  })

export default MypageFeatureTestScreen;