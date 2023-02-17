package examples.assignments;

import util.Util;

import java.math.BigInteger;

import circuit.config.Config;
import circuit.eval.CircuitEvaluator;
import circuit.structure.CircuitGenerator;
import circuit.structure.Wire;
import examples.gadgets.hash.MerkleTreePathGadget;
import examples.gadgets.hash.SHA256Gadget;
import examples.gadgets.hash.SubsetSumHashGadget;

public class Trade extends CircuitGenerator {

	private Wire[] publicRootWires;
	private Wire[] intermediateHashedWires;
	private Wire directionSelectorWire;
	private Wire[] leafWires;
	private int leafNumOfWords = 10;
	private int leafWordBitWidth = 32;
	private int treeHeight;
	private int hashDigestDimension = SubsetSumHashGadget.DIMENSION;
	
	private MerkleTreePathGadget merkleTreePathGadget;
	
	private String send = "Alice";
	private String recv = "Bob";
	private Wire[] sWire;
	private Wire[] rWire;
	private Wire product;
	private Wire[] pWire;
	private Wire[] concat;
	private SHA256Gadget sha256Gadget;
	private Wire[] realHash;

	public Trade(String circuitName, int treeHeight) {	// 생성자
		super(circuitName);
		this.treeHeight = treeHeight;
	}

	@Override
	protected void buildCircuit() {	// Circuit 생성

		// 공개: commitment, rootwire
		// 비공개(witness): sender, receiver, product, leaf nodes, internal nodes, ...

		realHash = createInputWireArray(8);

		sWire = createProverWitnessWireArray(send.length());
		rWire = createProverWitnessWireArray(recv.length());
		
		product = createProverWitnessWire();
		pWire = product.getBitWires(256).asArray();

		concat = Util.concat(Util.concat(sWire, rWire), pWire);

		sha256Gadget = new SHA256Gadget(concat, 8, concat.length, false, true);

		publicRootWires = createInputWireArray(hashDigestDimension, "Input Merkle Tree Root");
		intermediateHashedWires = createProverWitnessWireArray(hashDigestDimension * treeHeight, "Intermediate Hashes");
		directionSelectorWire = createProverWitnessWire("Direction selector");
		leafWires = createProverWitnessWireArray(leafNumOfWords, "Secret Leaf");


		// merkleTreePathGadget = new MerkleTreePathGadget(directionSelectorWire, data가 들어가야함!!, intermediateHashedWires, leafWordBitWidth, treeHeight);
		merkleTreePathGadget = new MerkleTreePathGadget(directionSelectorWire, leafWires, intermediateHashedWires, leafWordBitWidth, treeHeight);
		Wire[] actualRoot = merkleTreePathGadget.getOutputWires();

		
		Wire errorAccumulator = getZeroWire();
		for(int i = 0; i < hashDigestDimension; i++) {
			Wire diff = actualRoot[i].sub(publicRootWires[i]);
			Wire check = diff.checkNonZero();
			errorAccumulator = errorAccumulator.add(check);
		}
			
		Wire[] hashOutput = sha256Gadget.getOutputWires();
		Wire error = getZeroWire();
		for (int i = 0; i < 8; i++) {
			error = error.add(leafWires[i].isEqualTo(realHash[i]));
		}
		
		makeOutputArray(hashOutput, "test_hash");
		makeOutput(error.checkNonZero(), "Comparison of hash");
		
		// 여기서는 subsetsum을 이용하기 때문에 root가 매우 길다.
		makeOutputArray(actualRoot, "Computed Root");
		makeOutput(errorAccumulator.checkNonZero(), "Comparison of root");
	}

	@Override
	public void generateSampleInput(CircuitEvaluator circuitEvaluator) {

		circuitEvaluator.setWireValue(product, Util.nextRandomBigInteger(Config.FIELD_PRIME));

		for (int i = 0; i < send.length(); i++) {
			circuitEvaluator.setWireValue(sWire[i], send.charAt(i));
		}
		for (int i = 0; i < recv.length(); i++) {
			circuitEvaluator.setWireValue(rWire[i], recv.charAt(i));
		}


		// for(int i = 0; i < hashDigestDimension; i++) {
		// 	circuitEvaluator.setWireValue(publicRootWires[i], Util.nextRandomBigInteger(Config.FIELD_PRIME));
		// }
		
		circuitEvaluator.setWireValue(directionSelectorWire, Util.nextRandomBigInteger(treeHeight));
		for(int i = 0; i < hashDigestDimension * treeHeight; i++) {
			circuitEvaluator.setWireValue(intermediateHashedWires[i], Util.nextRandomBigInteger(Config.FIELD_PRIME));
		}
		
		for(int i = 0; i < leafNumOfWords; i++) {
			circuitEvaluator.setWireValue(leafWires[i], Integer.MAX_VALUE);
		}
		
		circuitEvaluator.setWireValue(realHash[0], new BigInteger("2565735471"));
        circuitEvaluator.setWireValue(realHash[1], new BigInteger("3923894777"));
        circuitEvaluator.setWireValue(realHash[2], new BigInteger("967912850"));
        circuitEvaluator.setWireValue(realHash[3], new BigInteger("339765561"));
        circuitEvaluator.setWireValue(realHash[4], new BigInteger("3236263565"));
        circuitEvaluator.setWireValue(realHash[5], new BigInteger("113730004"));
        circuitEvaluator.setWireValue(realHash[6], new BigInteger("2005499356"));
        circuitEvaluator.setWireValue(realHash[7], new BigInteger("2658935085"));
		
		circuitEvaluator.setWireValue(publicRootWires[0], new BigInteger("21326887438710555249204155190032483881661076067466575871418640836502221253773"));
		circuitEvaluator.setWireValue(publicRootWires[1], new BigInteger("15609469053083885453799602034235532483613973240946386498331380543244399980304"));
		circuitEvaluator.setWireValue(publicRootWires[2], new BigInteger("4982250979521535960996746118682827021377863781322608944815921871055205931575"));
	}

	public static void main(String[] args) throws Exception {

		Trade generator = new Trade("assignment - Song Jaeheon", 5);
		generator.generateCircuit();
		generator.evalCircuit();
		generator.prepFiles();
		generator.runLibsnark();
	}

}
