/**
 * 检验邮箱地址是否合法
 * @param email
 * @returns {boolean}
 */
export function isEmailValid(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

/**
 * 检验密码是否合法
 * @param password
 * @returns {boolean}
 */
export function isPasswordValid(password) {
    // 密码必须包含大小写字母、数字，且至少8位
    return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{8,}$/.test(password);
}