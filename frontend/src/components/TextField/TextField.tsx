import React, { useState } from "react";
import { TextInput, View, Text, StyleSheet } from "react-native";
import { TextFieldProps, TextFieldVariant } from "./TextField.types";
import { styles } from "./TextField.styles";
import { Icons } from "../../../assets/icons/Icons";

const TextField = ({
  label,
  value,
  onChangeText,
  placeholder,
  keyboardType = "default",
  secureTextEntry = false,
  disabled = false,
  error,
  variant = TextFieldVariant.Default,
  guide,
}: TextFieldProps) => {
  const [inputHeight, setInputHeight] = useState(51); // input 창 초기 높이 설정
  const [isFocused, setIsFocused] = useState(false);

  const getContainerStyle = () => {
    const containerStyles = [styles.container];

    if (variant === TextFieldVariant.Multiline) {
      containerStyles.push(styles.containerMultiline as never);
      // 멀티라인에서는 고정 높이 제거하고 동적 높이 적용
      containerStyles.push({
        height: undefined,
        minHeight: inputHeight,
      } as never);
    }

    if (disabled) {
      containerStyles.push(styles.containerDisabled as never);
    }

    if (error) {
      containerStyles.push(styles.containerError as never);
    }

    // 필드가 선택되었을 때 테두리 색상 변경
    if (
      isFocused &&
      (variant === TextFieldVariant.Default || secureTextEntry)
    ) {
      containerStyles.push(styles.containerFocused as never);
    }

    return StyleSheet.flatten(containerStyles); // 스타일 병합
  };

  const handleContentSizeChange = (event: any) => {
    const newHeight = event.nativeEvent.contentSize.height;
    const maxHeight = 90; // 최대 높이 제한
    const minHeight = 51; // 최소 높이 제한
    // 최소 높이와 최대 높이를 고려하여 동적 높이 설정
    setInputHeight(Math.max(minHeight, Math.min(newHeight, maxHeight)));
  };

  const getInputStyle = () => {
    const inputStyles: any[] = [styles.input];

    if (variant === TextFieldVariant.Multiline) {
      inputStyles.push({ height: inputHeight }); // Multiline일 때만 동적 높이 적용
    }

    if (disabled) {
      inputStyles.push(styles.inputDisabled as never);
    }

    if (error) {
      inputStyles.push(styles.inputError as never);
    }
    // 스타일 배열을 단일 객체로 병합
    return StyleSheet.flatten(inputStyles);
  };

  return (
    <View style={styles.fieldContainer}>
      {label && <Text style={styles.label}>{label}</Text>}
      <View style={getContainerStyle()}>
        <TextInput
          style={getInputStyle()}
          value={value}
          onChangeText={onChangeText}
          placeholder={placeholder}
          placeholderTextColor={styles.placeholder.color}
          keyboardType={keyboardType}
          secureTextEntry={secureTextEntry}
          editable={!disabled}
          multiline={variant === TextFieldVariant.Multiline}
          onFocus={() => setIsFocused(true)} // 포커스 시 상태 변경
          onBlur={() => setIsFocused(false)} // 포커스 해제 시 상태 변경
          onContentSizeChange={
            variant === TextFieldVariant.Multiline
              ? handleContentSizeChange
              : undefined
          } // Multiline일 때만 크기 변경 핸들러 사용
          textAlignVertical={
            variant === TextFieldVariant.Multiline ? "top" : "center"
          } // Multiline 일 때 상단 정렬 (Android 옵션, iOS는 기본적용 되어있음)
        />
      </View>
      {error ? (
        <Text style={styles.errorText}>{error}</Text>
      ) : guide ? (
        <Text style={styles.guideText}>{guide}</Text>
      ) : null}
    </View>
  );
};

export default TextField;
