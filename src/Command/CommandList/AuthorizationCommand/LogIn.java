package Command.CommandList.AuthorizationCommand;

import Command.CollectionManager.CollectionManager;
import Command.CommandList.CommandWithFlat;
import Command.CommandList.CommandsWithUser;
import Command.CommandProcessor.Command;
import Exceptions.IllegalKeyException;
import Exceptions.IllegalValueException;
import Exceptions.NoSuchCommandException;
import PossibleClassInCollection.Flat.User;

import java.io.Serializable;

public class LogIn implements Command, Serializable, CommandsWithUser {
    private User user;
    private CollectionManager cm;

    public LogIn(User user) {
        this.user=user;
    }
    public LogIn(){}

    @Override
    public String getName() {
        return "LOGIN";
    }

    @Override
    public String getDescription() {
        return "null";
    }

    @Override
    public void setArgument(String argument) throws NoSuchCommandException {
    }

    @Override
    public String getArgument() {
        return null;
    }

    @Override
    public CollectionManager getCm() {
        return cm;
    }

    @Override
    public void setCm(CollectionManager cm) {
            this.cm=cm;
    }

    @Override
    public String execute(String args) throws NoSuchCommandException, IllegalKeyException, IllegalValueException {
        return cm.loginUser(user);
    }

    @Override
    public void setUser(User user) {
        this.user=user;
    }

    @Override
    public User getUser() {
        return user;
    }
}
