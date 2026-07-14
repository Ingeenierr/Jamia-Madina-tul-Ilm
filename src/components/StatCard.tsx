import React, { useEffect, useState } from 'react';
import { StyleSheet, Text, View, Pressable } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { GlassCard } from './GlassCard';
import { type, useTheme } from '@/theme';

interface Props {
  label: string;
  value: number;
  icon: keyof typeof Ionicons.glyphMap;
  delay?: number;
  accent?: string;
  onPress?: () => void;
}

/** Dashboard tile with the ledger-number counting up from 0 on mount. */
export function StatCard({ label, value, icon, delay = 0, accent, onPress }: Props) {
  const { colors } = useTheme();
  const accentColor = accent ?? colors.gold;
  const [display, setDisplay] = useState(0);

  // Count up from 0 to `value` using an eased JS timer, staggered by `delay`
  // so a row of stat cards animates left-to-right rather than all at once.
  useEffect(() => {
    let raf: ReturnType<typeof setTimeout>;
    const start = Date.now() + delay;
    const duration = 700;
    const tick = () => {
      const elapsed = Date.now() - start;
      if (elapsed < 0) {
        raf = setTimeout(tick, 16);
        return;
      }
      const t = Math.min(1, elapsed / duration);
      setDisplay(Math.round(value * (1 - Math.pow(1 - t, 3))));
      if (t < 1) raf = setTimeout(tick, 16);
    };
    tick();
    return () => clearTimeout(raf);
  }, [value, delay]);

  const Wrapper = onPress ? Pressable : View;

  return (
    <GlassCard delay={delay} glow style={styles.card}>
      <Wrapper onPress={onPress} style={styles.pressable}>
        <View style={[styles.iconWrap, { backgroundColor: accentColor + '22' }]}>
          <Ionicons name={icon} size={20} color={accentColor} />
        </View>
        <Text style={[type.ledgerNumberL, { color: colors.textPrimary, marginTop: 10 }]}>{display}</Text>
        <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>{label}</Text>
      </Wrapper>
    </GlassCard>
  );
}

const styles = StyleSheet.create({
  card: { flex: 1, minWidth: 150 },
  pressable: { padding: 2 },
  iconWrap: {
    width: 38,
    height: 38,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
