import React, { useEffect } from 'react';
import { StyleSheet, View, ViewStyle } from 'react-native';
import { BlurView } from 'expo-blur';
import { LinearGradient } from 'expo-linear-gradient';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withDelay,
  withTiming,
  Easing,
} from 'react-native-reanimated';
import { radius, elevation, useTheme } from '@/theme';

interface Props {
  children: React.ReactNode;
  style?: ViewStyle;
  delay?: number;
  tone?: 'auto' | 'dark';
  glow?: boolean; // adds the gold "candle glow" top edge
}

/**
 * The app's signature surface: a frosted glass card with a hairline gold
 * top border. tone="auto" follows the current theme (light glass on a
 * light background, dark glass on a dark background); tone="dark" forces
 * the dark-glass treatment regardless of theme — used on the always-dark
 * dusk header/login screen.
 */
export function GlassCard({ children, style, delay = 0, tone = 'auto', glow = false }: Props) {
  const { colors, isDark } = useTheme();
  const useDarkGlass = tone === 'dark' || isDark;

  const opacity = useSharedValue(0);
  const translateY = useSharedValue(14);

  useEffect(() => {
    opacity.value = withDelay(delay, withTiming(1, { duration: 480, easing: Easing.out(Easing.cubic) }));
    translateY.value = withDelay(delay, withTiming(0, { duration: 480, easing: Easing.out(Easing.cubic) }));
  }, []);

  const animatedStyle = useAnimatedStyle(() => ({
    opacity: opacity.value,
    transform: [{ translateY: translateY.value }],
  }));

  return (
    <Animated.View
      style={[
        animatedStyle,
        elevation.card,
        styles.wrap,
        { borderColor: colors.hairline },
        style,
      ]}
    >
      {glow && (
        <LinearGradient
          colors={colors.candleGlow as unknown as string[]}
          style={styles.glow}
          start={{ x: 0.5, y: 0 }}
          end={{ x: 0.5, y: 1 }}
        />
      )}
      <BlurView
        intensity={useDarkGlass ? 55 : 40}
        tint={useDarkGlass ? 'dark' : 'light'}
        style={[
          StyleSheet.absoluteFillObject,
          { backgroundColor: useDarkGlass ? colors.glassDark : colors.glassSurface },
        ]}
      />
      <View style={styles.inner}>{children}</View>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    borderRadius: radius.lg,
    overflow: 'hidden',
    borderWidth: 1,
  },
  inner: { padding: 16 },
  glow: { position: 'absolute', top: 0, left: 0, right: 0, height: 40, zIndex: 2 },
});
