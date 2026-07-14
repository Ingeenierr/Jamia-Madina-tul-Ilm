import React from 'react';
import { StyleSheet, View, ViewStyle } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme } from '@/theme';

interface Props {
  children: React.ReactNode;
  variant?: 'dusk' | 'paper';
  style?: ViewStyle;
}

/**
 * Full-bleed backdrop. "dusk" = deep ink gradient used behind login and
 * dashboard headers in both light and dark mode. "paper" = the reading
 * surface behind list/detail screens — warm cream in light mode, deep
 * ink in dark mode.
 */
export function GradientBackground({ children, variant = 'paper', style }: Props) {
  const { colors } = useTheme();
  const colorsArr = variant === 'dusk' ? colors.duskGradient : colors.paperGradient;
  return (
    <View style={[styles.fill, { backgroundColor: colors.bgPaper }, style]}>
      <LinearGradient colors={colorsArr as unknown as string[]} style={StyleSheet.absoluteFill} start={{ x: 0.1, y: 0 }} end={{ x: 0.9, y: 1 }} />
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  fill: { flex: 1 },
});
