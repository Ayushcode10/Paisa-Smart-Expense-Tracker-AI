import { Tabs } from "expo-router";
import React, { useRef } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Animated,
  Dimensions,
} from "react-native";
import type { BottomTabBarProps } from "@react-navigation/bottom-tabs";

const { width } = Dimensions.get("window");

const C = {
  void: "#190019",
  deep: "#2B124C",
  mid: "#522B5B",
  dusk: "#854F6C",
  blush: "#DFB6B2",
  petal: "#FBE4D8",
};

const TABS = [
  { name: "home",         label: "Home",  icon: "⌂" },
  { name: "transactions", label: "Txns",  icon: "₹" },
];

function TabBar({ state, descriptors, navigation }: BottomTabBarProps) {
  return (
    <View style={styles.barWrapper}>
      <View style={styles.bar}>
        {state.routes.map((route, index) => {
          const focused = state.index === index;
          const tab = TABS.find((t) => t.name === route.name) ?? TABS[0];
          const scale = useRef(new Animated.Value(1)).current;

          const onPress = () => {
            Animated.sequence([
              Animated.timing(scale, { toValue: 0.8, duration: 80, useNativeDriver: true }),
              Animated.spring(scale, { toValue: 1, tension: 200, friction: 8, useNativeDriver: true }),
            ]).start();
            if (!focused) navigation.navigate(route.name);
          };

          return (
            <TouchableOpacity
              key={route.key}
              onPress={onPress}
              activeOpacity={1}
              style={styles.tabItem}
            >
              <Animated.View style={[styles.tabInner, { transform: [{ scale }] }]}>
                {focused && <View style={styles.activePill} />}
                <Text style={[styles.tabIcon, focused && styles.tabIconActive]}>
                  {tab.icon}
                </Text>
                <Text style={[styles.tabLabel, focused && styles.tabLabelActive]}>
                  {tab.label}
                </Text>
              </Animated.View>
            </TouchableOpacity>
          );
        })}
      </View>
    </View>
  );
}

export default function TabsLayout() {
  return (
    <Tabs
      tabBar={(props) => <TabBar {...props} />}
      screenOptions={{ headerShown: false }}
    >
      <Tabs.Screen name="home" />
      <Tabs.Screen name="transactions" />
    </Tabs>
  );
}

const styles = StyleSheet.create({
  barWrapper: {
    position: "absolute",
    bottom: 20,
    left: 16,
    right: 16,
    shadowColor: C.void,
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.6,
    shadowRadius: 20,
    elevation: 20,
  },
  bar: {
    flexDirection: "row",
    backgroundColor: "rgba(43,18,76,0.96)",
    borderRadius: 28,
    paddingVertical: 10,
    paddingHorizontal: 6,
    borderWidth: 1,
    borderColor: "rgba(251,228,216,0.12)",
    alignItems: "center",
  },
  tabItem: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
  tabInner: {
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 6,
    paddingHorizontal: 8,
    borderRadius: 16,
    position: "relative",
    minWidth: 44,
  },
  activePill: {
    position: "absolute",
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: "rgba(133,79,108,0.35)",
    borderRadius: 16,
    borderWidth: 1,
    borderColor: "rgba(223,182,178,0.2)",
  },
  tabIcon: {
    fontSize: 18,
    color: C.dusk,
    marginBottom: 2,
  },
  tabIconActive: {
    color: C.petal,
  },
  tabLabel: {
    fontSize: 9,
    color: C.dusk,
    fontWeight: "500",
    letterSpacing: 0.3,
  },
  tabLabelActive: {
    color: C.blush,
    fontWeight: "700",
  },
});