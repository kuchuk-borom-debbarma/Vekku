"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const cors_1 = __importDefault(require("cors"));
const body_parser_1 = __importDefault(require("body-parser"));
const BrainController_1 = require("./controllers/BrainController");
const BrainLogic_1 = require("./services/brain-logic/BrainLogic");
const config_1 = require("./config");
const app = (0, express_1.default)();
const PORT = config_1.config.port;
// Middleware
app.use((0, cors_1.default)());
app.use(body_parser_1.default.json());
// Routes
// POST because we are sending data (even if suggest is technically a read, we send a body)
app.post('/learn', BrainController_1.BrainController.Learn);
app.post('/raw-tags', BrainController_1.BrainController.GetRawTags);
app.post('/region-tags', BrainController_1.BrainController.GetRegionTags);
app.post('/score-tags', BrainController_1.BrainController.ScoreTags);
app.get('/tags', BrainController_1.BrainController.GetAllTags);
app.delete('/tags/:name', BrainController_1.BrainController.DeleteTag);
// Initialize AI/DB *before* accepting requests
const brain = BrainLogic_1.BrainLogic.getInstance();
brain.initialize().then(() => {
    app.listen(PORT, () => {
        console.log(`🤖 Vekku Brain Service (REST) running on port ${PORT}`);
    });
});
