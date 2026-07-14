import React, { useEffect, useMemo, useState } from 'react';
import { StyleSheet, View, Text, FlatList } from 'react-native';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { EmptyState } from '@/components/EmptyState';
import { SkeletonList } from '@/components/SkeletonList';
import { MonthStepper } from '@/components/MonthStepper';
import { type, useTheme } from '@/theme';
import { subscribeSalesForMonth } from '@/services/shopService';
import { formatCurrency, currentMonthKey } from '@/utils/format';
import { ShopSale } from '@/types/models';

export function ShopSalesHistoryScreen() {
  const { colors } = useTheme();
  const [month, setMonth] = useState(currentMonthKey());
  const [sales, setSales] = useState<ShopSale[] | null>(null);

  useEffect(() => {
    setSales(null);
    const unsub = subscribeSalesForMonth(month, setSales);
    return unsub;
  }, [month]);

  const total = useMemo(() => (sales ?? []).reduce((s, sale) => s + sale.totalAmount, 0), [sales]);

  return (
    <GradientBackground variant="paper">
      <ScreenHeader eyebrow="Shop sales" title={formatCurrency(total)} right={<MonthStepper month={month} onChange={setMonth} />} />

      {sales === null ? (
        <SkeletonList rows={5} />
      ) : (
        <FlatList
          data={sales}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.content}
          ListEmptyComponent={<EmptyState icon="receipt-outline" title="No sales this month" />}
          renderItem={({ item, index }) => (
            <GlassCard delay={Math.min(index, 10) * 40} style={styles.row}>
              <View style={{ flex: 1 }}>
                <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.itemName}</Text>
                <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>
                  {item.quantity} × {formatCurrency(item.unitPrice)} · {item.date}
                </Text>
              </View>
              <Text style={[type.ledgerNumber, { color: colors.statusApproved }]}>{formatCurrency(item.totalAmount)}</Text>
            </GlassCard>
          )}
        />
      )}
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: 20, paddingTop: 16, paddingBottom: 120 },
  row: { flexDirection: 'row', alignItems: 'center', marginBottom: 10 },
});
