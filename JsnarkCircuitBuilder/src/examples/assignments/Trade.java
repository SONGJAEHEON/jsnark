/*******************************************************************************
 * Author: Ahmed Kosba <akosba@cs.umd.edu>
 *******************************************************************************/
package examples.assignments;

import util.Util;
import circuit.config.Config;
import circuit.eval.CircuitEvaluator;
import circuit.structure.CircuitGenerator;
import circuit.structure.Wire;
import examples.gadgets.hash.MerkleTreePathGadget;
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

	public Trade(String circuitName, int treeHeight) {	// 생성자
		super(circuitName);
		this.treeHeight = treeHeight;
	}

	@Override
	protected void buildCircuit() {	// Circuit 생성

		publicRootWires = createInputWireArray(hashDigestDimension, "Input Merkle Tree Root");
		intermediateHashedWires = createProverWitnessWireArray(hashDigestDimension * treeHeight, "Intermediate Hashes");
		directionSelectorWire = createProverWitnessWire("Direction selector");
		leafWires = createProverWitnessWireArray(leafNumOfWords, "Secret Leaf");


		merkleTreePathGadget = new MerkleTreePathGadget(directionSelectorWire, leafWires, intermediateHashedWires, leafWordBitWidth, treeHeight);
		Wire[] actualRoot = merkleTreePathGadget.getOutputWires();

		Wire errorAccumulator = getZeroWire();
		for(int i = 0; i < hashDigestDimension; i++) {
			Wire diff = actualRoot[i].sub(publicRootWires[i]);
			Wire check = diff.checkNonZero();
			errorAccumulator = errorAccumulator.add(check);
		}

		makeOutputArray(actualRoot, "Computed Root");

		makeOutput(errorAccumulator.checkNonZero(), "Error if NON-zero");
	}

	@Override
	public void generateSampleInput(CircuitEvaluator circuitEvaluator) {
		String send = "Alice";
		String recv = "Bob";
		Wire product = new Wire(currentWireId);
		circuitEvaluator.setWireValue(product, Util.nextRandomBigInteger(Config.FIELD_PRIME));
		for(int i = 0; i < hashDigestDimension * treeHeight; i++) {
			circuitEvaluator.setWireValue(publicRootWires[i], Util.nextRandomBigInteger(Config.FIELD_PRIME));
		}

		circuitEvaluator.setWireValue(directionSelectorWire, Util.nextRandomBigInteger(treeHeight));
		for(int i = 0; i < hashDigestDimension * treeHeight; i++) {
			circuitEvaluator.setWireValue(intermediateHashedWires[i], Util.nextRandomBigInteger(Config.FIELD_PRIME));
		}

		for(int i = 0; i < leafNumOfWords; i++) {
			circuitEvaluator.setWireValue(leafWires[i], Integer.MAX_VALUE);
		}
	}

	public static void main(String[] args) throws Exception {

		Trade generator = new Trade("assignment - Song Jaeheon", 5);
		generator.generateCircuit();
		generator.evalCircuit();
		generator.prepFiles();
		generator.runLibsnark();
	}

}
