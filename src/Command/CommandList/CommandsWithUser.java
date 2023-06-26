package Command.CommandList;

import PossibleClassInCollection.Flat.User;

public interface CommandsWithUser  {
    User user = null;
    void setUser (User user);
    User getUser();
}
