export const validators = {
  number: '^[0-9]+$',
  alphanumeric: '^[a-zA-Zа-яА-Я0-9]+$',
  alphanumericwithhyphen: '^[a-zA-Z0-9-_]+$',
  emailPattern: '^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$',
  domainPattern: '^(((?!-))(xn--|_)?[a-z0-9-]{0,61}[a-z0-9]{1,1}\\.)*(xn--)?([a-z0-9][a-z0-9\\-]{0,60}|[a-z0-9-]{1,30}\\.[a-z]{2,})$'
};
