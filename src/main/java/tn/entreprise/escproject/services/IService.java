package tn.entreprise.escproject.services;

import java.util.List;

public interface IService <T>{
    public T add(T t);
    public T update(T t);
    public void delete(Long id);
    public T getById(Long id);
    public List<T> getAll();
    public List<T> addAll(List<T> ts);
}
