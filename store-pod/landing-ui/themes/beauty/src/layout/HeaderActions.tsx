'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {GlobeIcon, ShoppingBagIcon, UserIcon} from 'lucide-react';
import {Link, usePathname, useRouter} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import {useCart} from '@store-front/hooks/use-cart';
import {useUser} from '@store-front/hooks/use-user';
import {DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuTrigger} from '@store-front/ui/dropdown-menu';
import {cn} from '@store-front/ui/lib/utils';
import {CartDrawer} from './CartDrawer';

const plate = 'flex h-full w-11 items-center justify-center border-s border-foreground hover:bg-foreground hover:text-background focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-primary lg:w-12';
const menu = 'plate min-w-40 rounded-none border-foreground p-0 shadow-none';
const menuItem = 'rounded-none px-3 py-2 font-mono text-xs uppercase tracking-wide focus:bg-foreground focus:text-background';

export function HeaderActions({ctx}: { ctx: PageContext }) {
    return (
        <div className="flex items-stretch">
            <LocaleSwitcher ctx={ctx}/>
            <AccountButton ctx={ctx}/>
            <CartControl ctx={ctx}/>
        </div>
    );
}

function LocaleSwitcher({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER.LANGUAGE');
    const router = useRouter();
    const pathname = usePathname();
    const languages = ctx.store.supportedLanguages ?? [];
    if (languages.length <= 1) return null;
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild><button type="button" className={plate} aria-label={t('CHANGE_LANGUAGE')}><GlobeIcon className="size-4"/></button></DropdownMenuTrigger>
            <DropdownMenuContent align="end" className={menu}>
                <DropdownMenuGroup>
                    {languages.map(lang => (
                        <DropdownMenuItem key={lang} disabled={lang === ctx.locale} onClick={() => router.replace(pathname, {locale: lang})} className={menuItem}>{t(lang.toUpperCase())}</DropdownMenuItem>
                    ))}
                </DropdownMenuGroup>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}

function AccountButton({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const {user, login, logout} = useUser(ctx.storeContext);
    if (!user) return <button type="button" className={plate} aria-label={t('LOGIN')} onClick={login}><UserIcon className="size-4"/></button>;
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild><button type="button" className={plate} aria-label={t('ACCOUNT')}><UserIcon className="size-4"/></button></DropdownMenuTrigger>
            <DropdownMenuContent align="end" className={menu}>
                <DropdownMenuGroup>
                    <DropdownMenuItem asChild className={menuItem}><Link prefetch={false} href="/customer">{t('PROFILE')}</Link></DropdownMenuItem>
                    <DropdownMenuItem onClick={logout} className={cn(menuItem, 'text-destructive focus:bg-destructive focus:text-destructive-foreground')}>{t('LOGOUT')}</DropdownMenuItem>
                </DropdownMenuGroup>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}

function CartControl({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const pathname = usePathname();
    const {count} = useCart(ctx.storeContext);
    const [openPath, setOpenPath] = useState<string | null>(null);
    const open = openPath === pathname;
    const setOpen = (o: boolean) => setOpenPath(o ? pathname : null);
    // The tag swings once per count change: state adjusted during render, the animation restarts via key.
    const [prevCount, setPrevCount] = useState(count);
    const [swingKey, setSwingKey] = useState(0);
    if (count !== prevCount) { setPrevCount(count); if (count > 0) setSwingKey(k => k + 1); }
    return (
        <>
            <button type="button" aria-label={t('CART_WITH_COUNT', {count})} onClick={() => setOpen(true)}
                    key={swingKey} className={cn('tag ms-2 my-2 flex items-center gap-2 rounded-control ps-3 font-display text-sm font-semibold uppercase leading-none tracking-wide hover:bg-primary-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary', swingKey > 0 && 'tag-swing')}>
                <ShoppingBagIcon className="size-4"/>
                <span className="font-mono tabular-nums">{String(count).padStart(2, '0')}</span>
            </button>
            <CartDrawer ctx={ctx} open={open} onOpenChange={setOpen}/>
        </>
    );
}
