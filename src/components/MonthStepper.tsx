import React from 'react';
import { StyleSheet, View, Text, Pressable } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { type, useTheme } from '@/theme';
import { friendlyMonth, isCurrentMonth, shiftMonthKey } from '@/utils/format';

export function MonthStepper({ month, onChange }: { month: string; onChange: (m: string) => void }) {
  const { colors } = useTheme();
  return (
    <View style={styles.row}>
      <Pressable onPress={() => onChange(shiftMonthKey(month, -1))} hitSlop={10} style={styles.btn}>
        <Ionicons name="chevron-back" size={16} color={colors.textOnDark} />
      </Pressable>
      <Text style={[type.bodyS, { color: colors.textOnDark, minWidth: 96, textAlign: 'center' }]}>
        {friendlyMonth(month)}
      </Text>
      <Pressable
        onPress={() => onChange(shiftMonthKey(month, 1))}
        hitSlop={10}
        style={styles.btn}
        disabled={isCurrentMonth(month)}
      >
        <Ionicons name="chevron-forward" size={16} color={isCurrentMonth(month) ? 'rgba(252,245,229,0.3)' : colors.textOnDark} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: 2 },
  btn: { width: 26, height: 26, alignItems: 'center', justifyContent: 'center' },
});
