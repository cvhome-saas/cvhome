'use client'
import {useEffect, useState} from 'react';
import {useTranslations} from 'next-intl';
import {GlobeIcon, ShoppingBagIcon, UserIcon} from 'lucide-react';
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
                <Button variant="ghost" size="icon" aria-label={t('CHANGE_LANGUAGE')}><GlobeIcon/></Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
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
    const [open, setOpen] = useState(false);
    const pathname = usePathname();
    const {count} = useCart(ctx.storeContext);
    useEffect(() => setOpen(false), [pathname]);
    return (
        <>
            <Button variant="ghost" size="icon" className="relative" aria-label={t('CART_WITH_COUNT', {count})} onClick={() => setOpen(true)}>
                <ShoppingBagIcon/>
                {count > 0 && (
                    <span aria-hidden className="absolute -end-0.5 -top-0.5 flex min-w-5 items-center justify-center rounded-badge bg-primary px-1 text-[0.65rem] font-semibold leading-5 text-primary-foreground">
                        {count}
                    </span>
                )}
            </Button>
            <CartDrawer ctx={ctx} open={open} onOpenChange={setOpen}/>
        </>
    );
}
