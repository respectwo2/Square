import { TypeResult } from "../types/typeResult";

export type StackParamList = {
    // isAfterSurvey - 설문조사 이후 나오는 성향 결과 페이지인가? givenNickname - API 호출 시 사용되는 닉네임 정보, typeResult - isAfterSurvey가 true일 때 넘겨지는 유저 성향 데이터. 
    PersonalityResultScreen: { isAfterSurvey : boolean, givenNickname : string, typeResult : TypeResult | null};
    ProfileUpdateScreen: undefined;
    MypageScreen: undefined;
    DeleteAccountScreen: undefined;
    UseAuthTestScreen: undefined;
    SignupTestScreen: undefined;
    ModalTestScreen: undefined;
    MypageFeatureTestScreen: undefined;
    PersonalitySurveyPage: undefined;
    LandingScreen: undefined;
};