import { TouchableOpacity, Text, ViewStyle, TextStyle } from "react-native";
import { ButtonProps } from "./Button.types";
import { styles } from "./Button.styles";

const Button = ({
  label,
  disabled = false,
  onPress,
  variant = "primary",
}: ButtonProps) => {
  /**
   * variant나 disabled 여부에 따라 container 스타일을 동적으로 구성
   */
  const getContainerStyle = (): ViewStyle[] => {
    const containerStyles: ViewStyle[] = [styles.container];

    // 비활성화 상태
    if (disabled) {
      containerStyles.push(styles.containerDisabled);
    } else if (variant === "secondary") {
      // variant가 secondary이면 다른 배경색
        containerStyles.push(styles.containerSecondary);
      }
      // 다른 variant가 있다면 여기에 추가
    return containerStyles;
  };
  const getLabelStyle = (): TextStyle[] => {
    const labelStyles: TextStyle[] = [styles.label];

    // 비활성화 상태
    if (disabled) {
      labelStyles.push(styles.labelDisabled);
    }
    // variant별 텍스트 색상 등을 다르게 하고 싶다면 여기에 추가
    return labelStyles;
  };

  return (
    <TouchableOpacity
      style={getContainerStyle()}
      onPress={onPress}
      disabled={disabled}
    >
      <Text style={styles.label}>{label}</Text>
    </TouchableOpacity>
  );
};

export default Button;
