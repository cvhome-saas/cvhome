'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {ShoppingBagIcon, UserIcon} from 'lucide-react';
import {Link, usePathname, useRouter} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import {useCart} from '@store-front/hooks/use-cart';
import {useUser} from '@store-front/hooks/use-user';
import {Button} from '@store-front/ui/button';
import {DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuTrigger} from '@store-front/ui/dropdown-menu';
import {CartDrawer} from './CartDrawer';
import {useMounted} from '../components/use-mounted';

export function HeaderActions({ctx}: { ctx: PageContext }) {
    return (
        <>
            <LocaleSwitcher ctx={ctx}/>
            <AccountButton ctx={ctx}/>
            <CartControl ctx={ctx}/>
        </>
    );
}

/** The catalogue's edition: the current language code as a printed tab, the other editions in a menu. */
function LocaleSwitcher({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER.LANGUAGE');
    const router = useRouter();
    const pathname = usePathname();
    const languages = ctx.store.supportedLanguages ?? [];
    if (languages.length <= 1) return null;
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="sm" className="display px-2 text-sm tracking-wide" aria-label={t('CHANGE_LANGUAGE')}>
                    <span lang="en" dir="ltr">{ctx.locale.toUpperCase()}</span>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                <DropdownMenuGroup>
                    {languages.map(lang => (
                        <DropdownMenuItem key={lang} disabled={lang === ctx.locale} onClick={() => router.replace(pathname, {locale: lang})}>
                            <span className="display w-7 text-xs text-muted-foreground" lang="en" dir="ltr">{lang.toUpperCase()}</span>
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
        return <Button variant="ghost" size="icon" aria-label={t('LOGIN')} onClick={login}><UserIcon/></Button>;
    }
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" aria-label={t('ACCOUNT')}><UserIcon/></Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                <DropdownMenuGroup>
                    <DropdownMenuItem asChild><Link prefetch={false} href="/customer">{t('PROFILE')}</Link></DropdownMenuItem>
                    <DropdownMenuItem onClick={logout} variant="destructive">{t('LOGOUT')}</DropdownMenuItem>
                </DropdownMenuGroup>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}

function CartControl({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const pathname = usePathname();
    const mounted = useMounted();
    const {count} = useCart(ctx.storeContext);
    const [openPath, setOpenPath] = useState<string | null>(null);
    const open = openPath === pathname;
    const setOpen = (o: boolean) => setOpenPath(o ? pathname : null);
    return (
        <>
            <Button variant="ghost" size="icon" className="relative" aria-label={t('CART_WITH_COUNT', {count})} onClick={() => setOpen(true)}>
                <ShoppingBagIcon/>
                {mounted && count > 0 && (
                    <span aria-hidden className="stamp absolute -end-0.5 -top-0.5 min-w-[1.125rem] justify-center px-1 py-0 text-[0.625rem] leading-[1.125rem]">
                        {count}
                    </span>
                )}
            </Button>
            <CartDrawer ctx={ctx} open={open} onOpenChange={setOpen}/>
        </>
    );
}
