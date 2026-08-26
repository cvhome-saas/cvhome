'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {GlobeIcon, UserIcon} from 'lucide-react';
import {Link, usePathname, useRouter} from '@store-front/i18n/navigation';
import type {PageContext} from '@store-front/theme';
import {useCart} from '@store-front/hooks/use-cart';
import {useUser} from '@store-front/hooks/use-user';
import {Button} from '@store-front/ui/button';
import {DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuTrigger} from '@store-front/ui/dropdown-menu';
import {CartDrawer} from './CartDrawer';

export function HeaderActions({ctx}: { ctx: PageContext }) {
    return (
        <>
            <LocaleSwitcher ctx={ctx}/>
            <AccountButton ctx={ctx}/>
            <OrderTally ctx={ctx}/>
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
                <Button variant="ghost" size="icon" aria-label={t('CHANGE_LANGUAGE')}><GlobeIcon/></Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="border-2 border-foreground">
                <DropdownMenuGroup>
                    {languages.map(lang => (
                        <DropdownMenuItem key={lang} disabled={lang === ctx.locale} onClick={() => router.replace(pathname, {locale: lang})}>
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
            <DropdownMenuContent align="end" className="border-2 border-foreground">
                <DropdownMenuGroup>
                    <DropdownMenuItem asChild><Link prefetch={false} href="/customer">{t('PROFILE')}</Link></DropdownMenuItem>
                    <DropdownMenuItem onClick={logout} variant="destructive">{t('LOGOUT')}</DropdownMenuItem>
                </DropdownMenuGroup>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}

/**
 * The order tally, not a bag icon: the running count printed in the second plate the way a counter clerk
 * keeps it. Empty, it is an outlined box waiting for a first line.
 */
function OrderTally({ctx}: { ctx: PageContext }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const tc = useTranslations('COMMON');
    const pathname = usePathname();
    const {count} = useCart(ctx.storeContext);
    const [openPath, setOpenPath] = useState<string | null>(null);
    const open = openPath === pathname;
    const setOpen = (o: boolean) => setOpenPath(o ? pathname : null);
    return (
        <>
            <button type="button" onClick={() => setOpen(true)} aria-label={t('CART_WITH_COUNT', {count})}
                    className="fold h-10 gap-2 ps-3 pe-2 text-sm" data-active={count > 0 ? 'true' : undefined}>
                <span className="hidden sm:inline">{tc('ITEMS')}</span>
                <span className="press min-w-6 border border-current px-1 text-center text-sm leading-5 tabular-nums">{count}</span>
            </button>
            <CartDrawer ctx={ctx} open={open} onOpenChange={setOpen}/>
        </>
    );
}
