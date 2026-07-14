import React from 'react';
import { StyleSheet, Text, View, Pressable } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { type, useTheme } from '@/theme';

interface Props {
  eyebrow: string;
  title: string;
  onLogout?: () => void;
  right?: React.ReactNode;
}

/** Dusk-gradient header used at the top of every dashboard/list screen — stays dark in both light and dark app themes, since it's the "always ink" brand band. */
export function ScreenHeader({ eyebrow, title, onLogout, right }: Props) {
  const { colors } = useTheme();
  const insets = useSafeAreaInsets();
  return (
    <LinearGradient
      colors={colors.duskGradient as unknown as string[]}
      style={[styles.wrap, { paddingTop: insets.top + 14 }]}
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 1 }}
    >
      <View style={styles.row}>
        <View style={{ flex: 1 }}>
          <Text style={[type.label, { color: colors.gold }]}>{eyebrow.toUpperCase()}</Text>
          <Text style={[type.displayL, { color: colors.textOnDark, marginTop: 2 }]}>{title}</Text>
        </View>
        <View style={styles.actions}>
          {right}
          {onLogout && (
            <Pressable onPress={onLogout} hitSlop={10} style={styles.iconBtn}>
              <Ionicons name="log-out-outline" size={20} color={colors.textOnDark} />
            </Pressable>
          )}
        </View>
      </View>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  wrap: { paddingHorizontal: 20, paddingBottom: 22, borderBottomLeftRadius: 28, borderBottomRightRadius: 28 },
  row: { flexDirection: 'row', alignItems: 'flex-start' },
  actions: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  iconBtn: {
    width: 36,
    height: 36,
    borderRadius: 12,
    backgroundColor: 'rgba(255,255,255,0.08)',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
