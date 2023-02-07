/*******************************************************************************
 * Author: Ahmed Kosba <akosba@cs.umd.edu>
 *******************************************************************************/
package examples.generators;

import circuit.eval.CircuitEvaluator;
import circuit.structure.CircuitGenerator;
import circuit.structure.Wire;

public class SimpleCircuitGenerator extends CircuitGenerator {

	private Wire[] inputs;	// input wire를 정의

	public SimpleCircuitGenerator(String circuitName) {	// 생성자
		super(circuitName);
	}

	@Override
	protected void buildCircuit() {	// Circuit 생성

		// 길이가 4인 input wire 선언
		// instance가 아닌 witness를 넣고 싶다면 createProverWitnessWire 사용
		inputs = createInputWireArray(4);

		// r1 = inputs[0] + inputs[1] = 1 + 2 = 3
		Wire r1 = inputs[0].add(inputs[1]);

		// r2 = inputs[2] + inputs[3] = 3 * 4 = 12
		Wire r2 = inputs[2].mul(inputs[3]);
		
		// result = r1 * r2 = 3 * 12 = 36
		Wire result = r1.mul(r2);

		// makeOutput을 통해 result에 저장된 값을 출력하며 이 예제의 경우 36을 출력 시 참
		makeOutput(result);

		// instance가 아닌 witness를 넣고 싶다면 createProverWitnessWire를 사용
		// // declare input array of length 4.
		// inputs = createInputWireArray(4);

		// // r1 = in0 * in1
		// Wire r1 = inputs[0].mul(inputs[1]);

		// // r2 = in2 + in3
		// Wire r2 = inputs[2].add(inputs[3]);

		// // result = (r1+5)*(6*r2)
		// Wire result = r1.add(5).mul(r2.mul(6));

		// // mark the wire as output
		// makeOutput(result);

	}

	@Override
	public void generateSampleInput(CircuitEvaluator circuitEvaluator) {
		for(int i = 0; i < 4; i++) {
			// inputs[0]~inputs[3]에 1~4 대입
			circuitEvaluator.setWireValue(inputs[i], i+1);
		}

		// for (int i = 0; i < 4; i++) {
		// 	circuitEvaluator.setWireValue(inputs[i], i + 1);
		// }
	}

	public static void main(String[] args) throws Exception {

		SimpleCircuitGenerator generator = new SimpleCircuitGenerator("simple_example");
		generator.generateCircuit();
		generator.evalCircuit();
		generator.prepFiles();
		generator.runLibsnark();
	}

}
