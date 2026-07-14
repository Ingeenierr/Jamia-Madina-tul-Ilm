import React, { useEffect } from 'react';
import { StyleSheet, View, Pressable, Text, useWindowDimensions } from 'react-native';
import { BlurView } from 'expo-blur';
import { BottomTabBarProps } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';
import Animated, { useSharedValue, useAnimatedStyle, withSpring } from 'react-native-reanimated';
import * as Haptics from 'expo-haptics';
import { radius, useTheme } from '@/theme';

const ICONS: Record<string, keyof typeof Ionicons.glyphMap> = {
  Home: 'home',
  Students: 'people',
  Teachers: 'person',
  Approvals: 'checkmark-done',
  Leaves: 'calendar',
  Classes: 'school',
  Attendance: 'clipboard',
  More: 'ellipsis-horizontal',
};

/**
 * Floating pill nav that sits above the home indicator with a gold
 * indicator that spring-slides beneath the active tab. Always uses the
 * dark-glass treatment regardless of theme, matching the dusk header band.
 */
export function FloatingTabBar({ state, descriptors, navigation }: BottomTabBarProps) {
  const { colors } = useTheme();
  const { width } = useWindowDimensions();
  const barWidth = Math.min(width - 32, 480);
  const tabWidth = barWidth / state.routes.length;
  const indicatorX = useSharedValue(state.index * tabWidth);

  useEffect(() => {
    indicatorX.value = withSpring(state.index * tabWidth, { damping: 16, stiffness: 180 });
  }, [state.index, tabWidth]);

  const indicatorStyle = useAnimatedStyle(() => ({
    transform: [{ translateX: indicatorX.value }],
  }));

  return (
    <View style={styles.wrap} pointerEvents="box-none">
      <View style={[styles.bar, { width: barWidth, borderColor: colors.hairlineDark }]}>
        <BlurView intensity={60} tint="dark" style={StyleSheet.absoluteFillObject} />
        <View style={[styles.tint, { backgroundColor: colors.glassDark }]} />
        <Animated.View style={[styles.indicator, { width: tabWidth - 12, backgroundColor: colors.gold }, indicatorStyle]} />
        {state.routes.map((route, index) => {
          const { options } = descriptors[route.key];
          const focused = state.index === index;
          const label = (options.tabBarLabel as string) ?? options.title ?? route.name;

          const onPress = () => {
            Haptics.selectionAsync();
            const event = navigation.emit({ type: 'tabPress', target: route.key, canPreventDefault: true });
            if (!focused && !event.defaultPrevented) navigation.navigate(route.name);
          };

          return (
            <Pressable key={route.key} onPress={onPress} style={[styles.tab, { width: tabWidth }]}>
              <Ionicons
                name={ICONS[route.name] ?? 'ellipse'}
                size={20}
                color={focused ? colors.inkGreen : colors.textOnDarkMuted}
              />
              {focused && (
                <Text numberOfLines={1} style={[styles.label, { color: colors.inkGreen }]}>
                  {label}
                </Text>
              )}
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { position: 'absolute', left: 0, right: 0, bottom: 20, alignItems: 'center' },
  bar: {
    flexDirection: 'row',
    height: 62,
    borderRadius: radius.pill,
    overflow: 'hidden',
    borderWidth: 1,
    alignItems: 'center',
  },
  tint: { ...StyleSheet.absoluteFillObject },
  indicator: {
    position: 'absolute',
    height: 44,
    left: 6,
    borderRadius: radius.pill,
  },
  tab: { alignItems: 'center', justifyContent: 'center', flexDirection: 'row', gap: 6, height: '100%' },
  label: { fontSize: 12.5, fontWeight: '700' },
});
