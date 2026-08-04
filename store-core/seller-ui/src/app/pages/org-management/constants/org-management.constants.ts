export const ORG_SIDEMENU_LINKS = [
  {
    id: '0',
    title: 'COMPONENTS.UPDATE_ORG',
    key: 'COMPONENTS.UPDATE_ORG',
    link: '/pages/org-management/org/{OrgId}'
  },
  {
    id: '1',
    title: 'COMPONENTS.CHANGE_PASSWORD',
    key: 'COMPONENTS.CHANGE_PASSWORD',
    link: '/pages/org-management/org/{OrgId}/change-password'
  },
  {
    id: '2',
    title: 'COMPONENTS.STORES',
    key: 'COMPONENTS.STORES',
    link: '/pages/org-management/org/{OrgId}/stores'
  }
];

export const PWD_PATTERN = '^(?=[^A-Z]*[A-Z])(?=[^a-z]*[a-z])(?=[^0-9]*[0-9]).{6,12}$';
export const EMAIL_PATTERN = '^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$';
