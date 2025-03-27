import { codeToText } from 'element-china-area-data';

/**
 * 解析地址对象为可读字符串
 * @param {string|object} address - 地址信息，可以是JSON字符串或对象
 * @returns {string} 格式化后的地址字符串
 */
export function parseAddress(address) {
  try {
    // 如果是字符串，尝试解析为对象
    const addressObj = typeof address === 'string' ? JSON.parse(address) : address;

    // 如果解析后不是对象，直接返回原始值
    if (!addressObj || typeof addressObj !== 'object') {
      return address || '';
    }

    // 解析区域代码为文本
    let regionText = '';
    if (Array.isArray(addressObj.region)) {
      regionText = addressObj.region
        .map(code => codeToText[code])
        .filter(text => text) // 过滤掉undefined或空值
        .join('');
    }

    // 组合详细地址
    const detailAddress = addressObj.detailAddress || '';

    // 返回完整地址
    return regionText + detailAddress;
  } catch (error) {
    console.error('地址解析失败:', error);
    return address || '';
  }
}

/**
 * 格式化地址对象
 * @param {object} addressData - 地址数据对象
 * @returns {object} 标准化的地址对象
 */
export function formatAddressObject(addressData) {
  return {
    region: Array.isArray(addressData.region) ? addressData.region : [],
    fullAddress: addressData.fullAddress || '',
    detailAddress: addressData.detailAddress || ''
  };
}