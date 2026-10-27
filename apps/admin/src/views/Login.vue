<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
    <div class="login-card">
      <div class="brand">
        <div class="brand-logo">🐾</div>
        <h1 class="brand-title">毛团子AI</h1>
        <p class="brand-subtitle">管理后台</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="账号" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="密码" type="password" size="large" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="submit">登录</el-button>
      </el-form>
      <p class="tip">默认账号 admin / admin123</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, FormInstance, FormRules } from "element-plus";
import { User, Lock } from "@element-plus/icons-vue";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const auth = useAuthStore();
const loading = ref(false);
const form = reactive({ username: "", password: "" });
const formRef = ref<FormInstance>();

const rules: FormRules = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
};

async function submit() {
  if (!formRef.value) return;
  await formRef.value.validate(async valid => {
    if (!valid) return;
    loading.value = true;
    try {
      await auth.login(form.username, form.password);
      ElMessage.success("登录成功");
      router.push("/dashboard");
    } catch (e) {} finally { loading.value = false; }
  });
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #FFF8F0 0%, #FFE4E1 100%);
  position: relative; overflow: hidden;
}
.login-bg { position: absolute; inset: 0; pointer-events: none; }
.circle { position: absolute; border-radius: 50%; filter: blur(40px); opacity: 0.6; }
.circle-1 { width: 400px; height: 400px; background: #FFB6C1; top: -100px; left: -100px; }
.circle-2 { width: 320px; height: 320px; background: #FFAA80; bottom: -80px; right: -80px; }
.circle-3 { width: 200px; height: 200px; background: #FFE4B5; top: 50%; left: 60%; }
.login-card {
  width: 380px;
  padding: 40px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.1);
  position: relative; z-index: 1;
}
.brand { text-align: center; margin-bottom: 32px; }
.brand-logo {
  width: 64px; height: 64px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #FFB6C1, #FFAA80);
  border-radius: 18px;
  display: flex; align-items: center; justify-content: center;
  font-size: 36px;
  box-shadow: 0 8px 20px rgba(255,182,193,0.4);
}
.brand-title { font-size: 24px; font-weight: 600; margin: 0 0 4px; }
.brand-subtitle { margin: 0; color: var(--color-text-secondary); font-size: 14px; }
.submit-btn { width: 100%; height: 44px; font-size: 15px; letter-spacing: 1px; }
.tip { text-align: center; margin-top: 16px; font-size: 12px; color: var(--color-text-tertiary); }
</style>