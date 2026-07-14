import React, { useEffect } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import Svg, { Rect, Line } from 'react-native-svg';
import Animated, { useSharedValue, useAnimatedProps, withDelay, withTiming, Easing } from 'react-native-reanimated';
import { type, useTheme } from '@/theme';
import { DayAttendanceSummary } from '@/services/attendanceService';

const AnimatedRect = Animated.createAnimatedComponent(Rect);

const CHART_HEIGHT = 110;
const BAR_WIDTH = 22;
const GAP = 14;
const SLOT = BAR_WIDTH + GAP;

function Bar({ x, heightPct, delay, color }: { x: number; heightPct: number; delay: number; color: string }) {
  const progress = useSharedValue(0);
  useEffect(() => {
    progress.value = withDelay(delay, withTiming(1, { duration: 600, easing: Easing.out(Easing.cubic) }));
  }, []);
  const animatedProps = useAnimatedProps(() => {
    const h = Math.max(4, heightPct * progress.value * CHART_HEIGHT);
    return { height: h, y: CHART_HEIGHT - h };
  });
  return <AnimatedRect x={x} width={BAR_WIDTH} rx={6} fill={color} animatedProps={animatedProps} />;
}

/**
 * Last-N-days present-rate bar chart. Smoothly animated, theme-aware, cheap
 * to build with a couple of react-native-svg primitives — a lot more
 * friction to get right in Compose Canvas.
 */
export function WeeklyAttendanceChart({ days }: { days: DayAttendanceSummary[] }) {
  const { colors } = useTheme();
  const width = days.length * SLOT;

  return (
    <View>
      <Svg width={width} height={CHART_HEIGHT + 24}>
        <Line x1={0} y1={CHART_HEIGHT} x2={width} y2={CHART_HEIGHT} stroke={colors.hairline} strokeWidth={1} />
        {days.map((d, i) => {
          const pct = d.total > 0 ? d.present / d.total : 0;
          const color = pct >= 0.75 ? colors.statusApproved : pct >= 0.4 ? colors.statusPending : colors.statusRejected;
          return <Bar key={d.date} x={i * SLOT + GAP / 2} heightPct={pct} delay={i * 60} color={color} />;
        })}
      </Svg>
      <View style={[styles.labelsRow, { width }]}>
        {days.map((d) => (
          <Text key={d.date} style={[type.bodyS, { color: colors.textMuted, width: SLOT, textAlign: 'center' }]}>
            {new Date(d.date + 'T00:00:00').toLocaleDateString(undefined, { weekday: 'short' }).slice(0, 2)}
          </Text>
        ))}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  labelsRow: { flexDirection: 'row', marginTop: 4 },
});
