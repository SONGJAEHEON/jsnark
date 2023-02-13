package examples.generators.hash;

import java.math.BigInteger;

import circuit.config.Config;
import circuit.eval.CircuitEvaluator;
import circuit.structure.CircuitGenerator;
import circuit.structure.Wire;
import examples.gadgets.hash.SHA256Gadget;
import util.Util;

public class shatest extends CircuitGenerator {
    private Wire[] inputWires;
    private SHA256Gadget sha256Gadget;

    private Wire[] test;
    private Wire[] realHash;

    public shatest(String circuitName) {
        super(circuitName);
    }

    @Override
    protected void buildCircuit() {
        // inputWires = createProverWitnessWireArray(8);
        // inputWires = createInputWire();
        inputWires = createProverWitnessWireArray(2);
        Wire[] a = inputWires[0].getBitWires(256).asArray();                           // bit 단위로 쪼개기
        Wire[] b = inputWires[1].getBitWires(256).asArray();
        Wire[] c = Util.concat(a, b);                                                           // 합치기 c = a || b

        sha256Gadget = new SHA256Gadget(c, 8, 512, false, true);
        /*  wire[]: hash할 array
            bitWidthPerInputElement: input의 원소의 bit 길이
            totalLengthInBytes: hash할 값의 byte 길이
            binaryOutput: 2진수로 출력
            paddingRequired: 글자 부족 시 padding이 필요한가?

            Assertion:
            1. 길이에 대한 정보가 올바른가?
               (length - 1) * bitWidthPerInputElement < totalLengthInBytes * 8 < length * bitWidthPerInputElement
            2. padding이 없을 때 모든 원소들이 채워져 있는가?
               !paddingRequired && totalLengthInBytes % 64 != 0 && length * bitWidthPerInputElement != totalLengthInBytes
         */ 
        Wire[] hashOutput = sha256Gadget.getOutputWires();
        makeOutputArray(hashOutput, "test_hash");

        test = createInputWireArray(26);
        realHash = createInputWireArray(8);
        sha256Gadget = new SHA256Gadget(test, 8, test.length, false, true);
        hashOutput = sha256Gadget.getOutputWires();
        Wire errorCheck = oneWire;
        for (int i = 0; i < hashOutput.length; i++) {
            errorCheck = errorCheck.mul(hashOutput[i].isEqualTo(realHash[i]));
        }
        makeOutput(errorCheck, "error if zero");

    }

    @Override
    public void generateSampleInput(CircuitEvaluator evaluator) {
        // for (int i = 0; i < inputWires.length; i++) {
        //     evaluator.setWireValue(inputWires[i], 2);
        // }
        evaluator.setWireValue(inputWires[0], Util.nextRandomBigInteger(Config.FIELD_PRIME));
        evaluator.setWireValue(inputWires[1], Util.nextRandomBigInteger(Config.FIELD_PRIME));
        String ch = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < ch.length(); i++) {
            evaluator.setWireValue(test[i], ch.charAt(i));
        }
        evaluator.setWireValue(realHash[0], new BigInteger("1908703455"));
        evaluator.setWireValue(realHash[1], new BigInteger("2480320047"));
        evaluator.setWireValue(realHash[2], new BigInteger("519754052"));
        evaluator.setWireValue(realHash[3], new BigInteger("2087110994"));
        evaluator.setWireValue(realHash[4], new BigInteger("1580294680"));
        evaluator.setWireValue(realHash[5], new BigInteger("3478256781"));
        evaluator.setWireValue(realHash[6], new BigInteger("2664968946"));
        evaluator.setWireValue(realHash[7], new BigInteger("3673262963"));
    }

    public static void main(String[] args) throws Exception {
        shatest generator = new shatest("test_sha2");
        generator.generateCircuit();
        generator.evalCircuit();
        generator.prepFiles();
        generator.runLibsnark();
    }
}
