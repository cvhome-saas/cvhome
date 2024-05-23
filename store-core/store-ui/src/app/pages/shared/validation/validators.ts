export const validators = {
  number: '^[0-9]+$',
  alphanumeric: '^[a-zA-Zа-яА-Я0-9]+$',
  alphanumericwithhyphen: '^[a-zA-Z0-9-_]+$',
  emailPattern: '^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$',
  domainPattern:'^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]\\.[a-z]{2,}$'
};
