import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { type, useTheme } from '@/theme';

export function EmptyState({
  icon = 'file-tray-outline',
  title,
  subtitle,
}: {
  icon?: keyof typeof Ionicons.glyphMap;
  title: string;
  subtitle?: string;
}) {
  const { colors } = useTheme();
  return (
    <View style={styles.wrap}>
      <View style={[styles.iconWrap, { backgroundColor: colors.goldSoft }]}>
        <Ionicons name={icon} size={26} color={colors.goldDeep} />
      </View>
      <Text style={[type.displayM, { color: colors.textPrimary, marginTop: 14 }]}>{title}</Text>
      {!!subtitle && (
        <Text style={[type.bodyM, { color: colors.textSecondary, marginTop: 6, textAlign: 'center' }]}>
          {subtitle}
        </Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { alignItems: 'center', paddingVertical: 48, paddingHorizontal: 32 },
  iconWrap: {
    width: 56,
    height: 56,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
