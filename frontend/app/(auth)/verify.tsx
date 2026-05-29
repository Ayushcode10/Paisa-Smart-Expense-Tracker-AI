import React, { useState, useRef, useEffect } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  Animated,
  ActivityIndicator,
  StatusBar,
  Dimensions,
} from "react-native";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter, useLocalSearchParams } from "expo-router";
import { authService } from "../../lib/services/authService";
//import { tokenStore } from "../../lib/tokenStore";
import * as SecureStore from 'expo-secure-store';

const { width } = Dimensions.get("window");

const C = {
  void: "#190019",
  deep: "#2B124C",
  mid: "#522B5B",
  dusk: "#854F6C",
  blush: "#DFB6B2",
  petal: "#FBE4D8",
};

const OTP_LENGTH = 6;

export default function VerifyScreen() {
  const router = useRouter();
  const { phone } = useLocalSearchParams<{ phone: string }>();

  const [otp, setOtp] = useState<string[]>(Array(OTP_LENGTH).fill(""));
  const [name, setName] = useState("");
  const [isNewUser, setIsNewUser] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [resendTimer, setResendTimer] = useState(30);
  const [canResend, setCanResend] = useState(false);

  const inputRefs = useRef<(TextInput | null)[]>(Array(OTP_LENGTH).fill(null));
  const shakeAnim = useRef(new Animated.Value(0)).current;
  const cardOpacity = useRef(new Animated.Value(0)).current;
  const cardY = useRef(new Animated.Value(32)).current;

  // Entrance animation
  useEffect(() => {
    Animated.parallel([
      Animated.timing(cardOpacity, { toValue: 1, duration: 500, useNativeDriver: true }),
      Animated.spring(cardY, { toValue: 0, tension: 65, friction: 11, useNativeDriver: true }),
    ]).start(() => inputRefs.current[0]?.focus());
  }, []);

  // Resend countdown
  useEffect(() => {
    if (resendTimer <= 0) { setCanResend(true); return; }
    const t = setTimeout(() => setResendTimer((s) => s - 1), 1000);
    return () => clearTimeout(t);
  }, [resendTimer]);

  const shake = () => {
    Animated.sequence([
      Animated.timing(shakeAnim, { toValue: 10, duration: 55, useNativeDriver: true }),
      Animated.timing(shakeAnim, { toValue: -10, duration: 55, useNativeDriver: true }),
      Animated.timing(shakeAnim, { toValue: 8, duration: 55, useNativeDriver: true }),
      Animated.timing(shakeAnim, { toValue: -8, duration: 55, useNativeDriver: true }),
      Animated.timing(shakeAnim, { toValue: 0, duration: 55, useNativeDriver: true }),
    ]).start();
  };

  // ── OTP box handlers ──────────────────────────────────
  const handleOtpChange = (val: string, idx: number) => {
    const digit = val.replace(/\D/g, "").slice(-1);
    const next = [...otp];
    next[idx] = digit;
    setOtp(next);
    setError("");
    if (digit && idx < OTP_LENGTH - 1) {
      inputRefs.current[idx + 1]?.focus();
    }
  };

  const handleKeyPress = (e: any, idx: number) => {
    if (e.nativeEvent.key === "Backspace" && !otp[idx] && idx > 0) {
      const next = [...otp];
      next[idx - 1] = "";
      setOtp(next);
      inputRefs.current[idx - 1]?.focus();
    }
  };

  // Auto-submit when all 6 filled
  useEffect(() => {
    if (otp.every((d) => d !== "")) handleVerify();
  }, [otp]);

  const handleVerify = async () => {
    const code = otp.join("");
    if (code.length < OTP_LENGTH) {
      setError("Enter all 6 digits");
      shake();
      return;
    }
    try {
      setLoading(true);
      const res = await authService.verifyOtp({
        phone: phone ?? "",
        otp: code,
        ...(isNewUser && name.trim() ? { name: name.trim() } : {}),
      });
      await SecureStore.setItemAsync('auth_token', res.token);
      if (res.isNewUser) {
        setIsNewUser(true);
        // If we just got isNewUser back and have no name yet, ask for it
        if (!name.trim()) {
          setLoading(false);
          setError("");
          return;
        }
      }
      router.replace("/(tabs)/home");
    } catch (err: any) {
      setError(err?.message ?? "Invalid OTP. Please try again.");
      shake();
      setOtp(Array(OTP_LENGTH).fill(""));
      inputRefs.current[0]?.focus();
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!canResend) return;
    try {
      setCanResend(false);
      setResendTimer(30);
      setOtp(Array(OTP_LENGTH).fill(""));
      setError("");
      await authService.sendOtp({ phone: phone ?? "" });
      inputRefs.current[0]?.focus();
    } catch {
      setError("Could not resend OTP. Try again.");
    }
  };

  const maskedPhone = phone ? `+91 ${phone.slice(3, 8)}XXXXX` : "";

  return (
    <View style={styles.root}>
      <StatusBar barStyle="light-content" />
      <LinearGradient
        colors={[C.void, C.deep, C.mid]}
        start={{ x: 0.15, y: 0 }}
        end={{ x: 0.85, y: 1 }}
        style={StyleSheet.absoluteFill}
      />
      <View style={[styles.blob, styles.blobTL]} />
      <View style={[styles.blob, styles.blobBR]} />

      <KeyboardAvoidingView
        style={styles.kav}
        behavior={Platform.OS === "ios" ? "padding" : "height"}
      >
        {/* Back */}
        <TouchableOpacity style={styles.back} onPress={() => router.back()}>
          <Text style={styles.backText}>← Back</Text>
        </TouchableOpacity>

        <Animated.View
          style={[
            styles.card,
            { opacity: cardOpacity, transform: [{ translateY: cardY }] },
          ]}
        >
          <LinearGradient
            colors={["rgba(251,228,216,0.10)", "rgba(251,228,216,0.04)"]}
            style={StyleSheet.absoluteFill}
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 1 }}
          />

          {/* Header */}
          <Text style={styles.title}>Verify OTP</Text>
          <Text style={styles.sub}>
            Sent to{" "}
            <Text style={styles.phoneHighlight}>{maskedPhone}</Text>
          </Text>

          {/* OTP Boxes */}
          <Animated.View
            style={[styles.otpRow, { transform: [{ translateX: shakeAnim }] }]}
          >
            {otp.map((digit, i) => (
              <View
                key={i}
                style={[
                  styles.otpBox,
                  digit ? styles.otpBoxFilled : null,
                  error ? styles.otpBoxError : null,
                ]}
              >
                <TextInput
                  ref={(r: TextInput | null) => { inputRefs.current[i] = r; }}
                  style={styles.otpInput}
                  value={digit}
                  onChangeText={(v) => handleOtpChange(v, i)}
                  onKeyPress={(e) => handleKeyPress(e, i)}
                  keyboardType="number-pad"
                  maxLength={1}
                  selectionColor={C.blush}
                  caretHidden
                />
              </View>
            ))}
          </Animated.View>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}

          {/* Name field for new users */}
          {isNewUser && (
            <Animated.View style={styles.nameWrapper}>
              <Text style={styles.nameLabel}>Welcome! What's your name?</Text>
              <View style={styles.nameInputWrapper}>
                <TextInput
                  style={styles.nameInput}
                  value={name}
                  onChangeText={setName}
                  placeholder="Arjun Sharma"
                  placeholderTextColor={C.dusk + "88"}
                  autoFocus
                  returnKeyType="done"
                  onSubmitEditing={handleVerify}
                  selectionColor={C.blush}
                />
              </View>
            </Animated.View>
          )}

          {/* Verify Button */}
          <TouchableOpacity
            onPress={handleVerify}
            disabled={loading}
            activeOpacity={0.82}
            style={styles.btnOuter}
          >
            <LinearGradient
              colors={[C.dusk, C.mid, C.deep]}
              start={{ x: 0, y: 0 }}
              end={{ x: 1, y: 1 }}
              style={styles.btn}
            >
              {loading ? (
                <ActivityIndicator color={C.petal} />
              ) : (
                <Text style={styles.btnText}>
                  {isNewUser && !name.trim() ? "Continue →" : "Verify & Sign in →"}
                </Text>
              )}
            </LinearGradient>
          </TouchableOpacity>

          {/* Resend */}
          <TouchableOpacity
            onPress={handleResend}
            disabled={!canResend}
            style={styles.resendRow}
          >
            <Text style={[styles.resendText, !canResend && styles.resendDisabled]}>
              {canResend
                ? "Resend OTP"
                : `Resend in ${resendTimer}s`}
            </Text>
          </TouchableOpacity>
        </Animated.View>
      </KeyboardAvoidingView>
    </View>
  );
}

const BOX_SIZE = (width - 48 - 5 * 10) / 6;

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: C.void },
  kav: { flex: 1, justifyContent: "center", paddingHorizontal: 24 },

  blob: { position: "absolute", borderRadius: 999, opacity: 0.18 },
  blobTL: {
    width: width * 0.7, height: width * 0.7,
    backgroundColor: C.mid, top: -width * 0.2, left: -width * 0.2,
  },
  blobBR: {
    width: width * 0.55, height: width * 0.55,
    backgroundColor: C.dusk, bottom: -width * 0.1, right: -width * 0.15,
  },

  back: { position: "absolute", top: 56, left: 24 },
  backText: { color: C.blush, fontSize: 15, fontWeight: "600" },

  card: {
    borderRadius: 28,
    borderWidth: 1,
    borderColor: "rgba(251,228,216,0.14)",
    padding: 28,
    overflow: "hidden",
    backgroundColor: "rgba(43,18,76,0.55)",
  },

  title: {
    fontSize: 26,
    fontWeight: "700",
    color: C.petal,
    fontFamily: Platform.OS === "ios" ? "Georgia" : "serif",
    marginBottom: 6,
  },
  sub: { fontSize: 13.5, color: C.blush, opacity: 0.8, marginBottom: 32 },
  phoneHighlight: { color: C.petal, fontWeight: "600" },

  // OTP
  otpRow: { flexDirection: "row", justifyContent: "space-between", marginBottom: 8 },
  otpBox: {
    width: BOX_SIZE,
    height: BOX_SIZE * 1.15,
    borderRadius: 12,
    borderWidth: 1.5,
    borderColor: "rgba(223,182,178,0.3)",
    backgroundColor: "rgba(25,0,25,0.45)",
    justifyContent: "center",
    alignItems: "center",
  },
  otpBoxFilled: { borderColor: C.blush },
  otpBoxError: { borderColor: "#ff6b8a" },
  otpInput: {
    color: C.petal,
    fontSize: 22,
    fontWeight: "700",
    textAlign: "center",
    width: "100%",
    height: "100%",
  },

  errorText: { color: "#ff8fa3", fontSize: 12.5, marginBottom: 10, marginLeft: 2 },

  // Name
  nameWrapper: { marginTop: 16, marginBottom: 4 },
  nameLabel: { color: C.blush, fontSize: 13, marginBottom: 10, opacity: 0.9 },
  nameInputWrapper: {
    borderRadius: 14,
    borderWidth: 1.5,
    borderColor: "rgba(223,182,178,0.3)",
    backgroundColor: "rgba(25,0,25,0.45)",
    paddingHorizontal: 16,
    height: 50,
    justifyContent: "center",
  },
  nameInput: { color: C.petal, fontSize: 16, letterSpacing: 0.3 },

  // Button
  btnOuter: {
    borderRadius: 14,
    marginTop: 20,
    shadowColor: C.dusk,
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.5,
    shadowRadius: 16,
    elevation: 10,
  },
  btn: { height: 54, borderRadius: 14, justifyContent: "center", alignItems: "center" },
  btnText: { color: C.petal, fontSize: 16, fontWeight: "700", letterSpacing: 0.8 },

  // Resend
  resendRow: { marginTop: 18, alignItems: "center" },
  resendText: { color: C.blush, fontSize: 13.5, fontWeight: "600" },
  resendDisabled: { color: C.dusk, opacity: 0.6 },
});