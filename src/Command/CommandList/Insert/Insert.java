package Command.CommandList.Insert;

import Command.CollectionManager.CollectionManager;
import Command.CommandList.CommandWithFlat;
import Command.CommandList.CommandsWithUser;
import Command.CommandProcessor.Command;
import Command.Parse.Filler;
import Exceptions.IllegalKeyException;
import Exceptions.IllegalValueException;
import Exceptions.NoSuchCommandException;
import PossibleClassInCollection.Flat.Flat;
import PossibleClassInCollection.Flat.User;

import java.io.Serializable;
import java.util.NoSuchElementException;

public class Insert implements Command, Serializable, CommandWithFlat, CommandsWithUser {
    private CollectionManager cm;
    private String argument;
    private Flat flat;
    private User user;
    public Insert(CollectionManager cm){
        this.cm=cm;
    }
    public Insert(){
    }
    @Override
    public void setArgument(String argument){
        //filler
        this.argument=argument;
    }

    @Override
    public String getArgument(){
        return argument;
    }
    @Override
    public String getName() {
        return "INSERT";
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент с заданным ключом";
    }
    public CollectionManager getCm() {
        return cm;
    }

    public void setCm(CollectionManager cm) {
        this.cm = cm;
    }

    @Override
    public String execute(String args) throws NoSuchElementException, NumberFormatException, IllegalKeyException, IllegalValueException,NoSuchCommandException {
        try {


            if (args.isEmpty()) {
                throw new NoSuchCommandException();
            } else if (cm.getCollection().containsKey(Long.valueOf(args))) {
                return ("уже есть квартира с таким номером");
            }
        }catch (NumberFormatException e){
            return "стринг не там где надо";
        }
        return cm.insert(this);
    }

    @Override
    public void setFlat() throws IllegalValueException {
        Filler pr = new Filler();
        flat = pr.parser(Long.parseLong(argument),user);
    }

    @Override
    public Flat getFlat() {
        return flat;
    }

    @Override
    public void setFlatScript(Flat flat) {
this.flat=flat;
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
