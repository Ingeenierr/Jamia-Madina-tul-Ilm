import React, { useEffect, useState } from 'react';
import { StyleSheet, View, Text, FlatList, Pressable, Modal, KeyboardAvoidingView, Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import Animated, { SlideInDown } from 'react-native-reanimated';
import { GradientBackground } from '@/components/GradientBackground';
import { ScreenHeader } from '@/components/ScreenHeader';
import { GlassCard } from '@/components/GlassCard';
import { AppTextInput } from '@/components/AppTextInput';
import { PrimaryButton } from '@/components/PrimaryButton';
import { StatusPill } from '@/components/StatusPill';
import { EmptyState } from '@/components/EmptyState';
import { SkeletonList } from '@/components/SkeletonList';
import { useToast } from '@/components/Toast';
import { type, useTheme } from '@/theme';
import { subscribeShopItems, addShopItem, recordSale } from '@/services/shopService';
import { formatCurrency } from '@/utils/format';
import { ShopItem } from '@/types/models';

export function ShopScreen({ navigation }: any) {
  const { colors } = useTheme();
  const { show } = useToast();
  const [items, setItems] = useState<ShopItem[] | null>(null);
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [sellItem, setSellItem] = useState<ShopItem | null>(null);
  const [quantity, setQuantity] = useState('1');
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ name: '', category: '', price: '', stock: '' });

  useEffect(() => subscribeShopItems(setItems), []);

  const handleAddItem = async () => {
    const price = parseFloat(form.price);
    const stock = parseInt(form.stock, 10);
    if (!form.name.trim()) {
      show('Please enter an item name.', 'error');
      return;
    }
    if (!price || price <= 0) {
      show('Please enter a valid price.', 'error');
      return;
    }
    if (isNaN(stock) || stock < 0) {
      show('Please enter a valid stock count.', 'error');
      return;
    }
    setSaving(true);
    try {
      await addShopItem({ name: form.name.trim(), category: form.category.trim() || 'General', price, stock });
      show('Item added.', 'success');
      setForm({ name: '', category: '', price: '', stock: '' });
      setAddModalOpen(false);
    } catch (e: any) {
      show(e.message ?? 'Could not add this item.', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleSell = async () => {
    if (!sellItem) return;
    const qty = parseInt(quantity, 10);
    if (!qty || qty <= 0) {
      show('Please enter a valid quantity.', 'error');
      return;
    }
    setSaving(true);
    try {
      await recordSale(sellItem, qty);
      show(`Sold ${qty} × ${sellItem.name}.`, 'success');
      setSellItem(null);
      setQuantity('1');
    } catch (e: any) {
      // ShopError messages (e.g. "only N left in stock") are already
      // user-friendly, so they're shown as-is rather than a generic message.
      show(e.message ?? 'Could not complete this sale.', 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <GradientBackground variant="paper">
      <ScreenHeader
        eyebrow={items ? `${items.length} items` : 'Loading'}
        title="Shop"
        right={
          <Pressable onPress={() => navigation.navigate('ShopSales')} hitSlop={10} style={styles.headerBtn}>
            <Ionicons name="receipt-outline" size={18} color={colors.textOnDark} />
          </Pressable>
        }
      />

      {items === null ? (
        <SkeletonList rows={5} />
      ) : (
        <FlatList
          data={items}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.content}
          ListEmptyComponent={<EmptyState icon="storefront-outline" title="No items yet" subtitle="Tap + to stock your first item." />}
          renderItem={({ item, index }) => (
            <GlassCard delay={Math.min(index, 10) * 40} style={styles.row}>
              <View style={{ flex: 1 }}>
                <Text style={[type.bodyL, { color: colors.textPrimary, fontWeight: '600' }]}>{item.name}</Text>
                <Text style={[type.bodyS, { color: colors.textSecondary, marginTop: 2 }]}>
                  {item.category} · {formatCurrency(item.price)}
                </Text>
              </View>
              <StatusPill label={`${item.stock} in stock`} tone={item.stock > 0 ? 'approved' : 'rejected'} />
              <Pressable
                onPress={() => {
                  setSellItem(item);
                  setQuantity('1');
                }}
                disabled={item.stock <= 0}
                style={[styles.sellBtn, { backgroundColor: colors.gold, opacity: item.stock <= 0 ? 0.4 : 1 }]}
              >
                <Text style={[type.bodyS, { color: colors.inkGreen, fontWeight: '700' }]}>Sell</Text>
              </Pressable>
            </GlassCard>
          )}
        />
      )}

      <Pressable style={[styles.fab, { backgroundColor: colors.gold, shadowColor: colors.shadow }]} onPress={() => setAddModalOpen(true)}>
        <Ionicons name="add" size={26} color={colors.inkGreen} />
      </Pressable>

      <Modal visible={addModalOpen} transparent animationType="none" onRequestClose={() => setAddModalOpen(false)}>
        <View style={styles.modalOverlay}>
          <Pressable style={StyleSheet.absoluteFill} onPress={() => setAddModalOpen(false)} />
          <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
            <Animated.View entering={SlideInDown.duration(320).springify().damping(18)}>
              <GlassCard style={styles.modalCard}>
                <Text style={[type.displayM, { color: colors.textPrimary, marginBottom: 16 }]}>New item</Text>
                <AppTextInput label="Item name" value={form.name} onChangeText={(v) => setForm((f) => ({ ...f, name: v }))} />
                <AppTextInput label="Category" value={form.category} onChangeText={(v) => setForm((f) => ({ ...f, category: v }))} />
                <View style={styles.twoCol}>
                  <AppTextInput label="Price (Rs)" value={form.price} onChangeText={(v) => setForm((f) => ({ ...f, price: v }))} keyboardType="decimal-pad" style={{ flex: 1 }} />
                  <AppTextInput label="Stock" value={form.stock} onChangeText={(v) => setForm((f) => ({ ...f, stock: v }))} keyboardType="number-pad" style={{ flex: 1 }} />
                </View>
                <PrimaryButton label="Add item" onPress={handleAddItem} loading={saving} />
                <PrimaryButton label="Cancel" variant="ghost" onPress={() => setAddModalOpen(false)} style={{ marginTop: 4 }} />
              </GlassCard>
            </Animated.View>
          </KeyboardAvoidingView>
        </View>
      </Modal>

      <Modal visible={!!sellItem} transparent animationType="none" onRequestClose={() => setSellItem(null)}>
        <View style={styles.modalOverlay}>
          <Pressable style={StyleSheet.absoluteFill} onPress={() => setSellItem(null)} />
          <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
            <Animated.View entering={SlideInDown.duration(320).springify().damping(18)}>
              <GlassCard style={styles.modalCard}>
                <Text style={[type.displayM, { color: colors.textPrimary, marginBottom: 4 }]}>Sell {sellItem?.name}</Text>
                <Text style={[type.bodyS, { color: colors.textMuted, marginBottom: 16 }]}>
                  {sellItem ? `${sellItem.stock} in stock · ${formatCurrency(sellItem.price)} each` : ''}
                </Text>
                <AppTextInput label="Quantity" value={quantity} onChangeText={setQuantity} keyboardType="number-pad" />
                <PrimaryButton label="Confirm sale" onPress={handleSell} loading={saving} />
                <PrimaryButton label="Cancel" variant="ghost" onPress={() => setSellItem(null)} style={{ marginTop: 4 }} />
              </GlassCard>
            </Animated.View>
          </KeyboardAvoidingView>
        </View>
      </Modal>
    </GradientBackground>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: 20, paddingTop: 16, paddingBottom: 120 },
  row: { flexDirection: 'row', alignItems: 'center', gap: 10, marginBottom: 10 },
  sellBtn: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 10 },
  headerBtn: {
    width: 36,
    height: 36,
    borderRadius: 12,
    backgroundColor: 'rgba(255,255,255,0.08)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  fab: {
    position: 'absolute',
    right: 20,
    bottom: 100,
    width: 56,
    height: 56,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.3,
    shadowRadius: 14,
    elevation: 8,
  },
  modalOverlay: { flex: 1, justifyContent: 'flex-end', backgroundColor: 'rgba(0,0,0,0.4)' },
  modalCard: { margin: 16, marginBottom: 24 },
  twoCol: { flexDirection: 'row', gap: 12 },
});
