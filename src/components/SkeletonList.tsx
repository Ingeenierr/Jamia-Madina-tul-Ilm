import React, { useEffect } from 'react';
import { StyleSheet, View } from 'react-native';
import Animated, { useSharedValue, useAnimatedStyle, withRepeat, withSequence, withTiming } from 'react-native-reanimated';
import { radius, useTheme } from '@/theme';

function ShimmerBlock({ style }: { style: any }) {
  const { colors } = useTheme();
  const opacity = useSharedValue(0.4);

  useEffect(() => {
    opacity.value = withRepeat(withSequence(withTiming(0.9, { duration: 650 }), withTiming(0.4, { duration: 650 })), -1, true);
  }, []);

  const animatedStyle = useAnimatedStyle(() => ({ opacity: opacity.value }));

  return <Animated.View style={[style, animatedStyle, { backgroundColor: colors.hairlineDark }]} />;
}

/** A row of skeleton cards shown while a list's first Firebase read is in flight. */
export function SkeletonList({ rows = 4 }: { rows?: number }) {
  const { colors } = useTheme();
  return (
    <View style={{ paddingHorizontal: 20, paddingTop: 16 }}>
      {Array.from({ length: rows }).map((_, i) => (
        <View
          key={i}
          style={[
            styles.card,
            { backgroundColor: colors.isDark ? 'rgba(255,255,255,0.03)' : 'rgba(1,50,32,0.03)', borderColor: colors.hairline },
          ]}
        >
          <ShimmerBlock style={styles.avatar} />
          <View style={{ flex: 1, marginLeft: 12 }}>
            <ShimmerBlock style={styles.lineWide} />
            <ShimmerBlock style={[styles.lineNarrow, { marginTop: 8 }]} />
          </View>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    borderRadius: radius.lg,
    borderWidth: 1,
    marginBottom: 10,
  },
  avatar: { width: 44, height: 44, borderRadius: 14 },
  lineWide: { height: 14, borderRadius: 7, width: '70%' },
  lineNarrow: { height: 11, borderRadius: 6, width: '45%' },
});
