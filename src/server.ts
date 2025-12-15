import express from 'express';
import cors from 'cors';
import bodyParser from 'body-parser';
import { BrainController } from './controllers/BrainController';
import { BrainLogic } from './services/BrainLogic';

const app = express();
const PORT = 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json());

// Routes
// POST because we are sending data (even if suggest is technically a read, we send a body)
app.post('/learn', BrainController.Learn);
app.post('/analyze', BrainController.Analyze);
app.post('/suggest-tags', BrainController.SuggestTags);

// Initialize AI/DB *before* accepting requests
const brain = BrainLogic.getInstance();

brain.initialize().then(() => {
    app.listen(PORT, () => {
        console.log(`🤖 Vekku Brain Service (REST) running on port ${PORT}`);
    });
});