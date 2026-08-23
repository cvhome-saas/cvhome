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
import {useMounted} from '../components/use-mounted';

const ICON_STRIP = 'strip strip-hover size-9 justify-center px-0 [--tilt:0deg]';
const MENU = 'sheet !rounded-none !border-0 p-1 [--tilt:0deg]';
const ITEM = 'rounded-none font-display text-sm uppercase tracking-wide focus:bg-foreground focus:text-background data-[highlighted]:bg-foreground data-[highlighted]:text-background';

export function HeaderActions({ctx}: { ctx: PageContext }) {
    return (
        <>
            <LocaleSwitcher ctx={ctx}/>
            <AccountButton ctx={ctx}/>
            <CartControl ctx={ctx}/>
        </>
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
            <DropdownMenuTrigger asChild>
                <button type="button" className={cn(ICON_STRIP, 'gap-1 px-2 sm:w-auto')} aria-label={t('CHANGE_LANGUAGE')}>
                    <GlobeIcon className="size-4"/><span className="hidden text-xs sm:inline">{ctx.locale.toUpperCase()}</span>
                </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className={MENU}>
                <DropdownMenuGroup>
                    {languages.map(lang => (
                        <DropdownMenuItem key={lang} disabled={lang === ctx.locale} className={ITEM} onClick={() => router.replace(pathname, {locale: lang})}>
                            {t(lang.toUpperCase())}
                        </DropdownMenuItem>
                    ))}
                </DropdownMenuGroup>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}

function AccountButton({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const {user, login, logout} = useUser(ctx.storeContext);
    if (!user) {
        return <button type="button" className={ICON_STRIP} aria-label={t('LOGIN')} onClick={login}><UserIcon className="size-4"/></button>;
    }
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <button type="button" className={ICON_STRIP} aria-label={t('ACCOUNT')}><UserIcon className="size-4"/></button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className={MENU}>
                <DropdownMenuGroup>
                    <DropdownMenuItem asChild className={ITEM}><Link prefetch={false} href="/customer">{t('PROFILE')}</Link></DropdownMenuItem>
                    <DropdownMenuItem onClick={logout} variant="destructive" className={ITEM}>{t('LOGOUT')}</DropdownMenuItem>
                </DropdownMenuGroup>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}

/** The day-glo stub: bag icon + count; the count slaps when it changes. */
function CartControl({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const pathname = usePathname();
    const mounted = useMounted();
    const {count: liveCount} = useCart(ctx.storeContext);
    const count = mounted ? liveCount : 0;
    const [openPath, setOpenPath] = useState<string | null>(null);
    const [bump, setBump] = useState(0);
    const [prevCount, setPrevCount] = useState<number | null>(null);  // null until the first post-mount count: hydration never bumps
    if (mounted && count !== prevCount) {
        setPrevCount(count);
        if (prevCount !== null && count > prevCount) setBump(b => b + 1);
    }
    const open = openPath === pathname;
    const setOpen = (o: boolean) => setOpenPath(o ? pathname : null);
    return (
        <>
            <button type="button" className="glo h-9 gap-1.5 px-2.5 text-sm [--tilt:0deg] sm:px-3" aria-label={t('CART_WITH_COUNT', {count})} onClick={() => setOpen(true)}>
                <ShoppingBagIcon className="size-4"/>
                <span key={bump} aria-hidden className={cn('min-w-[1.25ch] text-center tabular-nums', bump > 0 && 'bump')}>{count}</span>
            </button>
            <CartDrawer ctx={ctx} open={open} onOpenChange={setOpen}/>
        </>
    );
}
