import type { NextFunction, Request, Response } from "express";
import express from "express";
import {TemplateManager} from "./template-manager";

const port = 8110;
const dir = process.cwd();
const THEME_HEADER_NAME = 'Theme';
const DEFAULT_THEME_NAME = 'default';
const env: string = process.env.NODE_ENV || "development";

// Express app setup
const app = express();

// Template manager to load and cache Next.js apps per template
const templateManager = new TemplateManager(dir, env, port);


function getTheme(req: Request) {
    console.log(`Request Headers: ${JSON.stringify(req.headers)}`);
    const rawTheme = req.headers[THEME_HEADER_NAME];
    const themeName = Array.isArray(rawTheme) ? rawTheme[0] : rawTheme ?? DEFAULT_THEME_NAME;
    return themeName.toLowerCase();
}

app.get("*", async (req: Request, res: Response, next: NextFunction) => {
    try {

        const theme = getTheme(req);
        console.log(`ThemeName: ${theme} | Request URL: ${req.originalUrl}`);

        const nextApp = await templateManager.getApp(theme);
        const handle = nextApp.getRequestHandler();

        await handle(req, res);
    } catch (err) {
        next(err);
    }
});

app.listen(port, () => {
    console.log(`✅ Store Front running on http://localhost:${port} env: ${env}`);
});