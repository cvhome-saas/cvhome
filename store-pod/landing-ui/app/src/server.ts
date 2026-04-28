import './instrumentation';
import type {NextFunction, Request, Response} from "express";
import express from "express";
import {TemplateManager} from "./template-manager";
import path from "path";

const port = 8110;
const dir = process.cwd();
const THEME_HEADER_NAME = 'theme';
const DEFAULT_THEME_NAME = 'basis';
const env: string = process.env.NODE_ENV || "development";

// Express app setup
const app = express();

// Template manager to load and cache Next.js apps per template
const templateManager = new TemplateManager(dir, env, port);


function getTheme(req: Request) {
    const rawTheme = req.headers[THEME_HEADER_NAME];
    const themeName = Array.isArray(rawTheme) ? rawTheme[0] : rawTheme ?? DEFAULT_THEME_NAME;
    return themeName.toLowerCase();
}

function getLocaleFromCookie(req: Request): string | undefined {
    const cookieHeader = req.headers.cookie;
    if (!cookieHeader) return undefined;

    const cookies = cookieHeader.split(';').reduce((acc: Record<string, string>, cookie) => {
        const [name, ...rest] = cookie.split('=');
        acc[name.trim()] = rest.join('=').trim();
        return acc;
    }, {});

    return cookies['NEXT_LOCALE'];
}

app.get(/(.*)/, async (req: Request, res: Response, next: NextFunction) => {
    try {
        const storeId = req.headers['store-id'];
        if (storeId) {
            const defaultLanguage = req.headers['default-language'];
            const localeFromCookie = getLocaleFromCookie(req);
            const targetLanguage = localeFromCookie || defaultLanguage;

            if ((req.path === '/' || req.path === '') && targetLanguage) {
                res.redirect(`/${targetLanguage}`);
                return;
            }
            const theme = getTheme(req);
            const nextApp = await templateManager.getApp(theme);
            const handle = nextApp.getRequestHandler();
            await handle(req, res);
        } else {
            res.status(404).sendFile(path.join(dir, 'public', '404.html'));
        }
    } catch (err) {
        next(err);
    }
});

app.listen(port, () => {
    console.log(`✅ Store Front running on http://localhost:${port} env: ${env}`);
});