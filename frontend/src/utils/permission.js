// 角色权限配置
const rolePermissions = {
  admin: [
    'Dashboard',
    'Orders',
    'Stores',
    'Users',
    'Staff',
    'WorkLogs'
  ],
  staff: [
    'Dashboard',
    'Orders',
    'Stores',
    'WorkLogs'
  ],
  customer: [
    'Orders'
  ]
}

// 检查用户是否有权限访问指定路由
export const hasPermission = (role, routeName) => {
  if (!role || !routeName) return false
  return rolePermissions[role]?.includes(routeName) || false
}

// 获取用户可访问的路由
export const getAccessibleRoutes = (role) => {
  if (!role) return []
  return rolePermissions[role] || []
}

// 检查用户是否有权限访问指定功能
export const hasFeaturePermission = (role, feature) => {
  const featurePermissions = {
    admin: ['create', 'edit', 'delete', 'view', 'export'],
    staff: ['create', 'edit', 'delete', 'view'],
    customer: ['create', 'view']
  }
  
  return featurePermissions[role]?.includes(feature) || false
}
