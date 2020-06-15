package bootcamp;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;

import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

/* Our contract, governing how our state will evolve over time.
 * See src/main/java/examples/ArtContract.java for an example. */
public class TokenContract implements Contract {
    public static String ID = "bootcamp.TokenContract";


    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {

        CommandWithParties<TokenContract.Commands> command = requireSingleCommand(tx.getCommands(), TokenContract.Commands.class);

        List<ContractState> inputs = tx.getInputStates();
        List<ContractState> outputs = tx.getOutputStates();

        if (command.getValue() instanceof TokenContract.Commands.Issue) {

            requireThat(req -> {
                req.using("Transaction must have no input states", inputs.isEmpty());
                req.using("Transaction must have one output state", outputs.size() == 1);
                req.using("Transaction output must be a TokenState", outputs.get(0) instanceof TokenState);

                TokenState output = (TokenState) outputs.get(0);

                req.using("Issuer must be a required signer", command.getSigners().contains(output.getIssuer().getOwningKey()));
                req.using("Owner must be a required signer", command.getSigners().contains(output.getOwner().getOwningKey()));

                req.using("Amount must be positive.", output.getAmount() > 0);

                return null;
            });

        } else {
            throw new IllegalArgumentException("Unrecognized Command.");
        }
    }


    public interface Commands extends CommandData {
        class Issue implements Commands {
        }
    }
}
