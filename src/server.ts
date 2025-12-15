import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import path from 'path';
import { BrainController } from './controllers/BrainController';
import { BrainLogic } from './services/BrainLogic';

const PROTO_PATH = path.join(__dirname, '../protos/brain.proto');

const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
});

const brainProto = grpc.loadPackageDefinition(packageDefinition) as any;

const server = new grpc.Server();
// 1. Bind Controller
server.addService(brainProto.brain.BrainService.service, BrainController);

// 2. Start
const PORT = '50051';
const brain = BrainLogic.getInstance();

// Initialize AI/DB *before* accepting requests
brain.initialize().then(() => {
    server.bindAsync(`0.0.0.0:${PORT}`, grpc.ServerCredentials.createInsecure(), () => {
        console.log(`🤖 Vekku Brain Service running on port ${PORT}`);
    });
});