import React, { useState } from 'react';
import { StyleSheet, TextInput, TextInputProps, View, Text } from 'react-native';
import Animated, { useSharedValue, useAnimatedStyle, withTiming } from 'react-native-reanimated';
import { Ionicons } from '@expo/vector-icons';
import { radius, type, useTheme } from '@/theme';

interface Props extends TextInputProps {
  label: string;
  icon?: keyof typeof Ionicons.glyphMap;
  error?: string;
  tone?: 'auto' | 'dark';
}

export function AppTextInput({ label, icon, error, tone = 'auto', style, onFocus, onBlur, ...rest }: Props) {
  const { colors, isDark } = useTheme();
  const [, setFocused] = useState(false);
  const borderProgress = useSharedValue(0);
  const useDarkField = tone === 'dark' || isDark;

  const animatedBorder = useAnimatedStyle(() => ({
    borderColor: borderProgress.value ? colors.gold : useDarkField ? colors.hairlineDark : colors.hairline,
  }));

  return (
    <View style={styles.wrap}>
      <Text style={[type.label, { color: useDarkField ? colors.textOnDarkMuted : colors.textSecondary, marginBottom: 6 }]}>
        {label.toUpperCase()}
      </Text>
      <Animated.View
        style={[
          styles.inputRow,
          animatedBorder,
          { backgroundColor: useDarkField ? 'rgba(255,255,255,0.06)' : colors.surface },
        ]}
      >
        {icon && (
          <Ionicons name={icon} size={18} color={useDarkField ? colors.textOnDarkMuted : colors.textMuted} style={{ marginRight: 8 }} />
        )}
        <TextInput
          {...rest}
          onFocus={(e) => {
            setFocused(true);
            borderProgress.value = withTiming(1, { duration: 180 });
            onFocus?.(e);
          }}
          onBlur={(e) => {
            setFocused(false);
            borderProgress.value = withTiming(0, { duration: 180 });
            onBlur?.(e);
          }}
          placeholderTextColor={useDarkField ? 'rgba(252,245,229,0.4)' : colors.textMuted}
          style={[type.bodyL, styles.input, { color: useDarkField ? colors.textOnDark : colors.textPrimary }, style]}
        />
      </Animated.View>
      {!!error && <Text style={[type.bodyS, { color: colors.statusRejected, marginTop: 4 }]}>{error}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { marginBottom: 16 },
  inputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1.5,
    borderRadius: radius.sm,
    paddingHorizontal: 14,
    height: 52,
  },
  input: { flex: 1, height: '100%' },
});
